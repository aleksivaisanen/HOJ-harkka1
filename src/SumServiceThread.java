import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

public class SumServiceThread extends Thread {
	private int port;
	private int index;
	private int receivedNumbers = 0;
	private int sumOfReceivedNumbers = 0;
	int[] sumOfThread;
	int[] numberOfNumbers;
	
	
	public SumServiceThread(int port, int index, int[] sumOfThread, int[] numberOfNumbers){
		this.port = port;
		this.index = index;
		this.sumOfThread = sumOfThread;
		this.numberOfNumbers = numberOfNumbers;
	}
	
	
	public void run() {
		ServerSocket sSocket = null;
		Socket socket = null;
		InputStream iS = null;
		OutputStream oS = null;
		ObjectOutputStream oOut = null;
		ObjectInputStream oIn = null;
		//avataan yhteys palvelijan ja serverin välille;
		try {
			sSocket = new ServerSocket(port);
			socket = sSocket.accept();
			iS = socket.getInputStream();
			oS = socket.getOutputStream();
			oOut = new ObjectOutputStream(oS);
			oIn = new ObjectInputStream(iS);
		}catch(Exception e){
			e.printStackTrace();
		}
		
		//aloitetaan itse summaaminen
		try {
			int nextNumber = oIn.readInt();
			while(nextNumber != 0) {
				setSumOfReceivedNumbers(getSumOfReceivedNumbers()+nextNumber);
				setReceivedNumbers(getReceivedNumbers()+1);
//				System.out.println("Received numbers: "+getReceivedNumbers());
//				System.out.println("Sum of numbers: "+getSumOfReceivedNumbers());
//				System.out.println(this);
//				System.out.println("\n");
				sumOfThread[index] = getSumOfReceivedNumbers();
				numberOfNumbers[index] = getReceivedNumbers();
				nextNumber = oIn.readInt();
			}
			if(nextNumber==0) {
				sSocket.close();
				socket.close();
				interrupt();
			}
		}catch(Exception e) {
			e.printStackTrace();
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
