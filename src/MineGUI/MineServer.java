package MineGUI;

// enjoy mine game by dr.han
// ToDo list
// 1. statistic (#success, #fail)
// 2. prevent same trial


import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;


class MineServer {
    public static int inPort = 9999;
    public static Vector<Client> clients = new Vector<Client>();


    public static void main(String[] args) throws Exception {
        MineServer game = new MineServer();
    }

    public MineServer() throws Exception {
        System.out.println("Server start running ..");
        ServerSocket server = new ServerSocket(inPort);
        while (true) {
            Socket socket = server.accept();
            Client c = new Client(socket);
            clients.add(c);
        }
    }


    class Client extends Thread {
        Socket socket;
        PrintWriter out = null;
        BufferedReader in = null;
        int width=0, num_mine=0;
        Map map;
        JFrame frame;
        public Container cont;
        public JPanel p0, p1;
        public JTextField t0;
        public JButton[] buttons;



        public Client(Socket socket) throws Exception {
            System.out.println("\n"+socket.getInetAddress()+ "  join ");
            this.socket = socket;
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            initial();

            start();
        }


        public void initial() throws IOException {
            String msg = in.readLine();
            String[] arr = msg.split(",");
            width = Integer.parseInt(arr[0]);
            num_mine = Integer.parseInt(arr[1]);

            map = new Map(width, num_mine);


            frame = new JFrame();
            frame.setTitle("From "+socket.getInetAddress());
            frame.setSize(400,300);
            frame.setLocation(150,150);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            cont = frame.getContentPane();
            cont.setLayout(new FlowLayout());

            p0 = new JPanel();
            p0.setBackground(Color.CYAN);
            p1 = new JPanel();
            p1.setBackground(Color.YELLOW);
            t0 = new JTextField(10);
            t0.setText(num_mine+" mines");
            p0.add(t0);
            cont.add(p0);
            cont.add(p1);
            frame.setVisible(true);

            p1.setLayout(new GridLayout(width, width));
            buttons = new JButton[width*width];
            for (int i=0; i<width*width; i++) {
                int x = i/width;
                int y = i%width;
                if (map.checkMine(x, y)>=0)
                    buttons[i] = new JButton("M");
                else
                    buttons[i] = new JButton(" ");
                p1.add(buttons[i]);
            }
            cont.validate();
        }


        @Override
        public void run() {
            String msg;

            try {
                while(true) {
                    msg = in.readLine();
                    if (msg==null) continue;
                    if(msg.equalsIgnoreCase("done")) {
                        break;
                    }
                    String[] arr = msg.split(",");
                    int x = Integer.parseInt(arr[0]);
                    int y = Integer.parseInt(arr[1]);

                    int result = map.checkMine(x,y);
                    out.println(""+result);
                    if(result >= 0) {
                        map.updateMap(x, y);
                    }

                    if (result>=0)
                        buttons[x*width+y].setText("O");
                    else
                        buttons[x*width+y].setText("X");

                }

                System.out.println("Success found "+num_mine+" mines !");
                out.close();
                in.close();
                socket.close();
            }
            catch (IOException e) { }
        }


    }

}
