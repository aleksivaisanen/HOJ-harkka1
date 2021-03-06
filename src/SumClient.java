import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;


public class SumClient {
	
	public static void main(String[] args) throws Exception {
		//m��ritell��n osoitteet ja portit sek� l�hetet��n ensimm�inen paketti serverille
		InetAddress targetAddr = InetAddress.getLoopbackAddress();
		int targetPort = 3126;
		DatagramSocket socket = new DatagramSocket();
		//satunnainen vapaa portti, t�ss� tapauksessa valittiin 51873
		int localPort = 51873;
		byte[] data = Integer.toString(localPort).getBytes();	
		//m��ritell��n paketti ja l�hetet��n
		DatagramPacket packet = new DatagramPacket (data, data.length, targetAddr, targetPort);
		socket.send(packet);
		socket.close();
		
		//sokettien ja input/outputtien m��rittelyt
		ServerSocket sSocket = null;
		Socket clientSocket = null;
		InputStream iS = null;
		OutputStream oS = null;
		ObjectOutputStream oOut = null;
		ObjectInputStream oIn = null;
		
		/* Kokeillaan yhteyden avaamista viisi kertaa.
		 * Jos viides kerta ei viel�k��n onnistu, ohjelma suljetaan.
		 */
		
		for(int i=1; i<6; i++) {
			try {
				//avataan soketti 
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
				//nukutaan 5s ennen uutta yrityst�
				Thread.sleep(5000);
			}
		}
		
		/** Odotetaan kokonaislukua t palvelimelta.
		 * Lukua odotetaan 5 sekuntia, tarkistetaan sekunnin v�lein onko luku jo tullut.
		 * Mik�li 5 sekunnin p��st� lukua ei ole saatu, palautetaan serverille -1 ja suljetaan ohjelma.
		 * 
		 * @param portquantity luku t palvelimelta, kertoo haluttujen porttien m��r�n
		 */
		int portquantity = 0;
		for(int i =1; i<=6; i++) {
			try {
				
				//luetaan luku
				portquantity = oIn.readInt();
				//tarkistetaan viel�, ett� portquantity on v�lill� [2, 10]
				if(portquantity >= 2 && portquantity <=10) {
					System.out.println("Number t received");
					System.out.println("Number t: "+portquantity);
					break;
				}
				//mik�li ei ole, palautetaan serverille -1 ja lopetetaan ohjelma
				else {
					System.out.println("First number was not between 2 and 10, shutting down the client");
					oOut.writeInt(-1);
					oOut.flush();
					System.exit(0);
				}
			}
			//mik�li heitet��n exceptionia, yritet��n uudelleen kunnes 5 sek on t�ynn�.
			catch(Exception e){
				e.printStackTrace();
				System.out.println("\nWaiting for number t "+(5-i)+ " seconds.");
				//tarkistetaan onko jo odotettu 5 sekuntia ja jos on, niin palautetaan serverille -1 ja suljetaan ohjelma
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
		//t�h�n talletetaan jokaisen threadin sen hetkinen summa
		int[] sumOfThread = new int[portquantity];
		//t�h�n talletetaan jokaisen threadin sille v�litettyjen numeroiden kokonaism��r�;
		int[] numberOfNumbers = new int[portquantity];
		
		//m��ritell��n haluttavat portit joissa summauspalvelijat toimivat, sek� alustetaan summauspalvelijat
		for(int i = 0; i<portquantity; i++) {
			//valitaan portista 51000 alkaen serverin pyyt�m� m��r� portteja
			int port = 51000 + i;
			System.out.println("Port number "+(i+1)+ " = " + port);
			//alustetaan uusi summauspalvelija ja tallennetaan viittaus taulukkoon
			threads[i] = new SumServiceThread(port, i, sumOfThread, numberOfNumbers);
			//kerrotaan serverille mist� portista summauspalvelijan l�yt��
			oOut.writeInt(port);
			oOut.flush();	
			threads[i].start();
		}

		
		//kuunnellaan uteluita serverilt�, ja odotetaan arvoja 1,2,3 tai 0
		while(true) {
			int action = oIn.readInt();
			//asetetaan minuutin odotusaika yhteydenotolle, jos se ylittyy, suljetaan ohjelma
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
			
			//jos serveri utelee numeron 1 kanssa, palautetaan t�h�n menness� v�litettyjen lukujen kokonaissumma
			if(action == 1) {
				int sumOfSums=0;
				//lasketaan summien kokonaisumma jaetusta taulukosta
				for(int i=0; i<sumOfThread.length;i++) {
					sumOfSums = sumOfSums + sumOfThread[i];
				}
				//v�litet��n tieto serverille
				oOut.writeInt(sumOfSums);
				oOut.flush();
			}
			
			//jos serveri utelee numeron 2 kanssa, palautetaan summista suurin
			else if(action==2) {
				int biggestSum = sumOfThread[0];
				int sumIndex = 1;
				//etsit��n suurimman summan omaava palvelija taulukosta
				for(int i=0; i<threads.length;i++) {
					if(biggestSum < sumOfThread[i]) {
						biggestSum = sumOfThread[i];
						sumIndex = i+1;
					}
				}
				//v�litet��n tieto serverille
				oOut.writeInt(sumIndex);
				oOut.flush();
			}
			//jos serveri utelee numeron 3 kanssa, palautetaan kaikille summauspalvelimille v�litettyjen lukujen kokonaism��r�
			else if(action==3) {
				int totalReceived = 0;
				//lasketaan vastaanotettujen numeroiden yhteism��r� jaetusta taulukosta
				for(int i=0; i<numberOfNumbers.length;i++) {
					totalReceived = totalReceived + numberOfNumbers[i];
				}
				//v�litet��n tieto serverille
				oOut.writeInt(totalReceived);
				oOut.flush();
			}
			//jos l�hett�� sovellukselle numeron 0, lopetetaan palveluiden k�ytt� ja suljetaan sovellus
			else if(action==0) {
				for(int i=0; i<threads.length;i++) {
					//l�hetet��n threadeille k�sky keskeytt�� toiminta
					threads[i].interrupt();
				}
				System.out.print("Sums: ");
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
				//jos serveri l�hett�� mit� sattuu, parempi lopettaa clientin toiminta
				System.out.println("Error while communicating with server, client shutting down.");
				break;
			}
			
		}
		 

		} // main
	

}
