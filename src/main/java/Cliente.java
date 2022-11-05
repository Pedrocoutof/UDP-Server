import javax.xml.crypto.Data;
import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Scanner;


public class Cliente {

    public static int byteArrayToInt( byte[] data ) {
        int intValue = 0;
        for (byte b : data) {
            intValue = (intValue << 8) + (b & 0xFF);
        }
        return intValue;
    }

    public static byte[] intToByteArray( int data ) {
        byte[] result = new byte[4];
        result[0] = (byte) ((data & 0xFF000000) >> 24);
        result[1] = (byte) ((data & 0x00FF0000) >> 16);
        result[2] = (byte) ((data & 0x0000FF00) >> 8);
        result[3] = (byte) ((data & 0x000000FF) >> 0);
        return result;
    }

    public static void writeByteInFile( byte[] data ) throws IOException {
        File arquivo_final = new File("./arquivos-usuario/download.png");
        OutputStream outputStream = new FileOutputStream(arquivo_final);

        outputStream.write(data);

        System.out.println("Arquivo gerado!");
    }


    public static void main(String[] args)throws Exception {
        final int porta_servidor = 4444;
        final int porta_cliente = 5555;
        final int tempo_espera = 500; //(mls)


        DatagramSocket socket = new DatagramSocket(porta_cliente);
        InetAddress ia = InetAddress.getLocalHost();
        InetAddress server = InetAddress.getByAddress(ia.getAddress());

        System.out.println("Cliente iniciado");

        byte[] solicitaConexao = new byte[1];
        solicitaConexao[0] = 1;

        DatagramPacket packetEnvio = new DatagramPacket(solicitaConexao, solicitaConexao.length, server, porta_servidor);
        socket.send(packetEnvio);

        // region DE ARQUIVOS
        byte[] confirmaRecebimento = new byte[1];
        byte[] nArquivos = new byte[4];

        System.out.println("Aguardando numero de arquivos");
        DatagramPacket packetRecebido = new DatagramPacket(nArquivos, nArquivos.length);
        socket.receive(packetRecebido);

        confirmaRecebimento[0] = 1;
        packetEnvio = new DatagramPacket(confirmaRecebimento, confirmaRecebimento.length, server, porta_servidor);
        Thread.sleep(tempo_espera);
        socket.send(packetEnvio);

        // endregion

        int numeroDeArquivos = byteArrayToInt(packetRecebido.getData());

        // region NOME ARQUIVOS

        System.out.println("Arquivos encontrados no banco:");
        for(int i = 0; i < numeroDeArquivos; i++){
            byte[] nome = new byte[50];
            packetRecebido = new DatagramPacket(nome, nome.length);
            socket.receive(packetRecebido);
            System.out.println("[" + i + "] - " + new String(packetRecebido.getData()).trim());
        }
        System.out.println("Digite o codigo do arquivo que deseja fazer download.");
        Scanner scanner = new Scanner(System.in);
        int codigo = scanner.nextInt();
        System.out.println(codigo);

        packetEnvio = new DatagramPacket(intToByteArray(codigo), intToByteArray(codigo).length, server, porta_servidor);
        socket.send(packetEnvio);

        // endregion

        // region RECEBE O TAMANHO DO ARQUIVO SOLICITADO

        byte[] b_tamanhoArquivo = new byte[4];
        packetRecebido = new DatagramPacket(b_tamanhoArquivo, b_tamanhoArquivo.length);
        socket.receive(packetRecebido);

        int tamanhoArquivo = byteArrayToInt(b_tamanhoArquivo);
        // endregion

        // region RECEBE ARQUIVO


        if(tamanhoArquivo < 65507){
            byte[] arquivo = new byte[tamanhoArquivo];
            packetRecebido = new DatagramPacket(arquivo, arquivo.length);
            socket.receive(packetRecebido);
        }

        // endregion

        writeByteInFile(packetRecebido.getData());

        System.out.println("Finalizando cliente");

    }
}