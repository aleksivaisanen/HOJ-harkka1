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
		
		InetAddress targetAddr = InetAddress.getLoopbackAddress();
		int targetPort = 3126;
		DatagramSocket socket = new DatagramSocket();
		int localPort = 51873;
		byte[] data = Integer.toString(localPort).getBytes();	
		DatagramPacket packet = new DatagramPacket (data, data.length, targetAddr, targetPort);
		socket.send(packet);
		socket.close();
		ServerSocket sSocket = new ServerSocket(localPort);
		Socket clientSocket = sSocket.accept();
		System.out.println("connection made");
		
		
		InputStream iS = clientSocket.getInputStream();
		OutputStream oS = clientSocket.getOutputStream();
		ObjectOutputStream oOut = new ObjectOutputStream(oS);
		ObjectInputStream oIn = new ObjectInputStream(iS);
		
		//luetaan tarvittavien porttien lukum‰‰r‰
		int portquantity = oIn.readInt();
		System.out.println(portquantity);
		for(int i = 0; i<portquantity; i++) {
			int sumport = 51000 + i;
			System.out.println(sumport);
			oOut.writeInt(sumport);
			oOut.flush();		
		}
		 
		 

		} // main

}
