import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

//summauspalvelijaluokka
public class SumServiceThread extends Thread {
	private int port;
	private int index;
	private int receivedNumbers = 0;
	private int sumOfReceivedNumbers = 0;
	int[] sumOfThread;
	int[] numberOfNumbers;
	
	/**
	 * @param port portti, jossa kyseinen thread toimii
	 * @param index threads taulukon indeksi, t‰ll‰ voidaan tallentaa summat oikeisiin paikkoihin jaetuissa taulukoissa
	 * @param sumOfThread t‰m‰n threadin laskema summa
	 * @param numberOfNumbers t‰m‰n threadin vastaanottamien lukujen lukum‰‰r‰
	 */
	public SumServiceThread(int port, int index, int[] sumOfThread, int[] numberOfNumbers){
		this.port = port;
		this.index = index;
		this.sumOfThread = sumOfThread;
		this.numberOfNumbers = numberOfNumbers;
	}
	
	
	public void run() {
		//alustetaan soketit sek‰ inputit
		ServerSocket sSocket = null;
		Socket socket = null;
		InputStream iS = null;
		ObjectInputStream oIn = null;
		//avataan yhteys palvelijan ja serverin v‰lille;
		try {
			sSocket = new ServerSocket(port);
			socket = sSocket.accept();
			iS = socket.getInputStream();
			oIn = new ObjectInputStream(iS);
		}catch(Exception e){
			e.printStackTrace();
		}
		
		//aloitetaan itse summaaminen
		try {
			//luetaan objectinputstreamista seuraava integer ja
			//k‰yd‰‰n objectinputstreamia niin kauan l‰pi kunnes saavutetaan 0
			int nextNumber = oIn.readInt();
			while(nextNumber != 0){
				
				setSumOfReceivedNumbers(getSumOfReceivedNumbers()+nextNumber);
				setReceivedNumbers(getReceivedNumbers()+1);
				//tallennetaan arvot jaettuihin taulukoihin, jotta sovellus pystyy vastaamaan serverin uteluihin
				//varmistetaan, ett‰ vain yksi thread p‰‰see k‰siksi yhteen jaettuun taulukkoon kerralla
				synchronized(sumOfThread) {
					sumOfThread[index] = getSumOfReceivedNumbers();
				}
				//sama juttu kuin ylemp‰n‰
				synchronized(numberOfNumbers) {
					numberOfNumbers[index] = getReceivedNumbers();
				}
				nextNumber = oIn.readInt();
			}
			//kun 0 saavutetaan, suljetaan yhteys
			if(nextNumber==0) {
				sSocket.close();
				socket.close();
				interrupt();
			}
		}catch(Exception e) {
		}
	
		
	}
	//getterit ja setterit
	public int getPort() {
		return port;
	}
	public int getReceivedNumbers() {
		return receivedNumbers;
	}
	public int getSumOfReceivedNumbers() {
		return sumOfReceivedNumbers;
	}
	public int getIndex() {
		return index;
	}
	
	public void setReceivedNumbers(int numbers) {
		receivedNumbers = numbers;
	}
	public void setSumOfReceivedNumbers(int sum) {
		sumOfReceivedNumbers = sum;
	}
}
