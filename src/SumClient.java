import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class SumClient {
	
	public static void main(String[] args) throws Exception {
		//m‰‰ritell‰‰n kolme linkedblockingqueueta, joiden v‰lill‰ threadit pystyv‰t vaihtamaan tietoa
		
		
		
		//m‰‰ritell‰‰n osoitteet ja portit sek‰ l‰hetet‰‰n ensimm‰inen paketti serverille
		InetAddress targetAddr = InetAddress.getLoopbackAddress();
		int targetPort = 3126;
		DatagramSocket socket = new DatagramSocket();
		//satunnainen vapaa portti, t‰ss‰ tapauksessa valittiin 51873
		int localPort = 51873;
		byte[] data = Integer.toString(localPort).getBytes();	
		DatagramPacket packet = new DatagramPacket (data, data.length, targetAddr, targetPort);
		socket.send(packet);
		socket.close();
		
		//yritet‰‰n avataan soketti clientin ja serverin v‰lille, sek‰ avataan streamit
		ServerSocket sSocket = null;
		Socket clientSocket = null;
		InputStream iS = null;
		OutputStream oS = null;
		ObjectOutputStream oOut = null;
		ObjectInputStream oIn = null;
		
		/* Kokeillaan yhteyden avaamista viisi kertaa.
		 * Jos viides kerta ei viel‰k‰‰n onnistu, ohjelma suljetaan.
		 */
		
		for(int i=1; i<6; i++) {
			try {
				sSocket = new ServerSocket(localPort);
				clientSocket = sSocket.accept();
				System.out.println("Connection established");
				
				//avataan objectinput- ja objectoutputstreamit
				iS = clientSocket.getInputStream();
				oS = clientSocket.getOutputStream();
				oOut = new ObjectOutputStream(oS);
				oIn = new ObjectInputStream(iS);
				break;
				
			}
			catch(Exception e) {
				e.printStackTrace();
				System.out.println("Attempt number "+i);
				System.out.println("Trying "+(5-i)+" more times to establish a connection");
				if(i==5) {
					System.exit(0);
				}
				//nukutaan 5s ennen uutta yrityst‰
				Thread.sleep(5000);
			}
		}
		
		/** Odotetaan kokonaislukua t palvelimelta.
		 * Lukua odotetaan 5 sekuntia, tarkistetaan sekunnin v‰lein onko luku jo tullut.
		 * Mik‰li 5 sekunnin p‰‰st‰ lukua ei ole saatu, palautetaan serverille -1 ja suljetaan ohjelma.
		 * 
		 * @param portquantity luku t palvelimelta, kertoo haluttujen porttien m‰‰r‰n
		 */
		int portquantity = 0;
		for(int i =1; i<=6; i++) {
			try {
				
				//luetaan luku
				portquantity = oIn.readInt();
				//tarkistetaan viel‰, ett‰ portquantity on v‰lill‰ [2, 10]
				if(portquantity >= 2 && portquantity <=10) {
					System.out.println("Number t received");
					System.out.println("Number t: "+portquantity);
					break;
				}
				//mik‰li ei ole, palautetaan serverille -1 ja lopetetaan ohjelma
				else {
					System.out.println("First number was not between 2 and 10, shutting down the client");
					oOut.writeInt(-1);
					oOut.flush();
					System.exit(0);
				}
			}
			//mik‰li heitet‰‰n exceptionia, yritet‰‰n uudelleen kunnes 5 sek on t‰ynn‰.
			catch(Exception e){
				e.printStackTrace();
				System.out.println("\nWaiting for number t "+(5-i)+ " seconds.");
				//tarkistetaan onko jo odotettu 5 sekuntia, jos on niin palautetaan serverille -1 ja suljetaan ohjelma
				if(i==5) {
					System.out.println("Didn't receive number t, shutting down the client");
					oOut.writeInt(-1);
					oOut.flush();
					System.exit(0);
				}
				//odotetaan sekunti
				Thread.sleep(1000);
			}
		}	

		
		//tallennetaan summauspalvelimet taulukkoon
		SumServiceThread[] threads = new SumServiceThread[portquantity];
		//t‰h‰n talletetaan jokaisen threadin sen hetkinen summa
		int[] sumOfThread = new int[portquantity];
		//t‰h‰n talletetaan jokaisen threadin sille v‰litettyjen numeroiden kokonaism‰‰r‰;
		int[] numberOfNumbers = new int[portquantity];
		
		for(int i = 0; i<portquantity; i++) {
			int port = 51000 + i;
			System.out.println("Port number "+(i+1)+ " = " + port);
			threads[i] = new SumServiceThread(port, i, sumOfThread, numberOfNumbers);
			oOut.writeInt(port);
			oOut.flush();	
			threads[i].start();
		}

		
		//kuunnellaan uteluita serverilt‰, ja odotetaan arvoja 1,2,3 tai 0
		while(true) {
			int action = oIn.readInt();
			//asetetaan minuutin odotusaika, jos se ylittyy, suljetaan ohjelma
			try {
				clientSocket.setSoTimeout(60000);
			}catch(Exception e) {
				e.printStackTrace();
				for(int i=0; i<threads.length;i++) {
					threads[i].interrupt();
				}
				sSocket.close();
				clientSocket.close();
				System.exit(0);
			}
			
			//jos serveri utelee numeron 1 kanssa, palautetaan t‰h‰n menness‰ v‰litettyjen lukujen kokonaissumma
			if(action == 1) {
				int sumOfSums=0;
				for(int i=0; i<sumOfThread.length;i++) {
					sumOfSums = sumOfSums + sumOfThread[i];
				}
				System.out.println("Summien summat: "+sumOfSums);
				oOut.writeInt(sumOfSums);
				oOut.flush();
			}
			
			//jos serveri utelee numeron 2 kanssa, palautetaan summista suurin
			else if(action==2) {
				int biggestSum = sumOfThread[0];
				int sumIndex = 1;
				for(int i=0; i<threads.length;i++) {
					if(biggestSum < sumOfThread[i]) {
						biggestSum = sumOfThread[i];
						sumIndex = i+1;
					}
					else {
					}
				}
				System.out.println("Suurimman summan omaava palvelija: "+ sumIndex);
				oOut.writeInt(sumIndex);
				oOut.flush();
			}
			//jos serveri utelee numeron 3 kanssa, palautetaan kaikille summauspalvelimille v‰litettyjen lukujen kokonaism‰‰r‰
			else if(action==3) {
				int totalReceived = 0;
				for(int i=0; i<numberOfNumbers.length;i++) {
					totalReceived = totalReceived + numberOfNumbers[i];
				}
				System.out.println("Saatujen lukujen m‰‰r‰: "+ totalReceived);
				oOut.writeInt(totalReceived);
				oOut.flush();
			}
			//jos l‰hett‰‰ sovellukselle numeron 0, lopetetaan palveluiden k‰yttˆ ja suljetaan sovellus
			else if(action==0) {
				for(int i=0; i<threads.length;i++) {
					threads[i].interrupt();
				}
				System.out.print("Summat: ");
				for(int i = 0; i<sumOfThread.length; i++) {
					System.out.print(sumOfThread[i] + " ");
				}
				sSocket.close();
				clientSocket.close();
				System.exit(0);
			}
			//jos saadaan jotain muuta, palautetaan sovellukselle -1
			else {
				oOut.writeInt(-1);
				oOut.flush();
			}
			
		}
		 

		} // main
	

}
