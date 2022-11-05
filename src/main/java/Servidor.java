import java.io.File;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Servidor {

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



    public static void  main(String[] args) throws Exception {

        final int porta_cliente = 5555;
        final int porta_server = 4444;
        final int tempo_espera = 500; //(mls)

        // cria o socket
        DatagramSocket socket = new DatagramSocket(porta_server);
        InetAddress ia = InetAddress.getLocalHost();
        InetAddress cliente = InetAddress.getByAddress(ia.getAddress());

        // Calculo o numero de itens no banco de arquivos
        File banco_arq = new File("./banco-de-arquivos/");
        byte[] nArquivos = intToByteArray(banco_arq.listFiles().length);

        byte[] flag = new byte[1];
        // Espero alguem enviar um sinal de vida
        while(flag[0] == 0){
            DatagramPacket flagPacket = new DatagramPacket(flag, flag.length);
            socket.receive(flagPacket);
        }
        flag[0] = 0;
        System.out.println("Alguem conectou!");

        while(flag[0] == 0){
            DatagramPacket packetEnvio = new DatagramPacket(nArquivos, nArquivos.length, cliente,porta_cliente);
            Thread.sleep(tempo_espera);
            socket.send(packetEnvio);
            System.out.println("Pacote enviado!");

            DatagramPacket flagPacket = new DatagramPacket(flag, flag.length);
            socket.receive(flagPacket);
            System.out.println("Confirmacao de recebimento");
        }

        for(int i = 0 ; i < banco_arq.listFiles().length; i++){
            byte[] nome = new byte[50];
            Thread.sleep(tempo_espera);
            nome = new String (banco_arq.listFiles()[i].getName()).getBytes();

            DatagramPacket packetEnvio = new DatagramPacket(nome, nome.length, cliente, porta_cliente);
            socket.send(packetEnvio);
        }

        // COLETA O CODIGO DO USUARIO:
        byte[] codigo = new byte[4];
        DatagramPacket packetRecebido = new DatagramPacket(codigo, codigo.length);
        socket.receive(packetRecebido);
        int codigoEscolhido = byteArrayToInt(packetRecebido.getData());

        System.out.println("Codigo digitado pelo usuario: " + codigoEscolhido);

        File arquivoSelecionado = banco_arq.listFiles()[codigoEscolhido];
        byte[] arquivoTam = intToByteArray((int) Files.size(arquivoSelecionado.toPath()));
        System.out.println("Tamanho do arquivo selecionado: " + byteArrayToInt(arquivoTam));
        DatagramPacket packetEnvio = new DatagramPacket(arquivoTam, arquivoTam.length, cliente, porta_cliente);
        Thread.sleep(tempo_espera);
        socket.send(packetEnvio);

        int tamanhoArquivo = byteArrayToInt(arquivoTam);

        byte[] b_arquivo = Files.readAllBytes(Paths.get(arquivoSelecionado.getPath()));

        Thread.sleep(tempo_espera);

        if(tamanhoArquivo < 65507){
            packetEnvio = new DatagramPacket(b_arquivo, b_arquivo.length, cliente, porta_cliente);
            socket.send(packetEnvio);
        }

    }

}