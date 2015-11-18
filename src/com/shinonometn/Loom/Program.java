package com.shinonometn.Loom;

import com.shinonometn.Loom.common.Logger;
import com.shinonometn.Loom.common.Networks;
import com.shinonometn.Loom.common.Toolbox;
import com.shinonometn.Loom.connector.Shuttle;
import com.shinonometn.Loom.ui.MainForm;
import com.shinonometn.Pupa.ToolBox.HexTools;

import javax.swing.*;
import java.net.*;
import java.util.List;
import java.util.Scanner;
import java.util.Vector;

/**
 * Created by catten on 15/10/20.
 */
public class Program{
    private static boolean developerMode = false;

    public static boolean isDeveloperMode(){
        return developerMode;
    }

    public static void main(String[] args){
        Logger.log("System: " + Toolbox.getSystemName());
        if(args.length >= 2){
            if(args[1] != null && "-developerMode".toLowerCase().equals(args[1].toLowerCase())){
                developerMode = true;
            }
        }
        //Logger.outPrint = developerMode;
        Logger.log(developerMode ? "DeveloperMode on" +
                "\n\t\t!!! Warning !!!" +
                "\nDeveloper mode will record all user data(included account and password)" +
                "\nPlease remember to clear logs for protect your personal Data" : "DeveloperMode off");

        if(args.length > 0 && "-consoleMode".toLowerCase().equals(args[0].toLowerCase())){
            Logger.log("Loom Console Mode");
            LoomConsole();
            if(Logger.isWriteToFile()){
                Logger.closeLog();
            }
        }else if(args.length == 0 || "-graphicMode".toLowerCase().equals(args[0].toLowerCase())){
            Logger.log("Loom Graphic Mode");
            try{
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            }catch (Exception e){
                Logger.log(e.toString());
            }

            new MainForm();

            Vector<NetworkInterface> nf = Networks.getNetworkInterfaces(false);
            StringBuilder stringBuilder = new StringBuilder("\n\n");
            if(nf != null) {
                stringBuilder.append("Network Interfaces found:\n");
                for (NetworkInterface n : nf) {
                    try {
                        stringBuilder.append(String.format("[%s]%n", n.getDisplayName())).append("\n");
                        List<InterfaceAddress> list = n.getInterfaceAddresses();
                        for (InterfaceAddress ia : list) {
                            try {
                                stringBuilder.append(ia.getAddress()).append("\n");
                            } catch (Exception e) {
                                stringBuilder.append("null\n");
                            }
                        }
                        stringBuilder.append(HexTools.byte2HexStr(n.getHardwareAddress())).append("\n");
                        //System.out.println();
                    } catch (Exception e) {
                        stringBuilder.append("Null\n");
                    }
                    stringBuilder.append("\n\n");
                }
            }
            Logger.log(stringBuilder.toString());
        }
    }

    public static void LoomConsole(){
        try {
            System.out.println("Welcome to use Loom v0.4!\n");

            String ip;
            String username;
            String password;

            Shuttle shuttle;

            Scanner scanner = new Scanner(System.in);

            System.out.println("Input your IP address:");
            ip = scanner.next();
            System.out.println("Getting Network Interface with " + ip);
            InetAddress inetAddress = InetAddress.getByName(ip);
            //InetAddress serverInetAddress;
            NetworkInterface networkInterface = NetworkInterface.getByInetAddress(inetAddress);
            byte[] macAddress = networkInterface.getHardwareAddress();
            if (macAddress == null) {
                System.out.println("Network Interface Not Available...Exit.");
                return;
            }

            shuttle = new Shuttle(networkInterface, null);
            shuttle.developerMode = Program.isDeveloperMode();
            System.out.println("Prepared to login.");

            System.out.println("Please input your account");
            username = scanner.next();
            System.out.println("PIN Code?(Password)");
            password = scanner.next();

            shuttle.setUsername(username);
            shuttle.setPassword(password);
            System.out.println("Loom Start.");
            shuttle.start();

            do{
                System.out.println("If you want to get offline or exit program, Please input \"exit\"");
                if (!scanner.next().toLowerCase().equals("exit")){
                    if(scanner.next().equals("about")){
                        aboutMe();
                    }else System.out.println("If you want to get offline or exit program, Please input \"exit\"");
                }else{
                    shuttle.Offline();
                    while(shuttle.isOnline() || shuttle.isMessageListening());
                    return;
                }
            }while(shuttle.isAlive());

            if(!developerMode){
                System.out.println(
                        "Some error accorded. Restart program or run " +
                                "\n\"java -jar Loom.jar -consoleMode -developerMode\"" +
                                "\n to know more."
                );
            }
        }catch (SocketException | UnknownHostException e){
            Logger.log(e.toString());
        }
    }

    public static void aboutMe(){
        System.out.println("\n\n如果这个软件早出一年就好了" +
                "\n可惜这个软件出来命令行测试版本的时候，我已经是大二了。" +
                "\n\n互联网是现代社会的基本设施" +
                "\n回收点运营成本，那是情理之中" +
                "\n但是用这么肮脏的手段阻止我们使用路由器以及共享网络给手机，那是不合道理的" +
                "\n浪费了一个多月的时间就是为了写这个软件" +
                "\n垃圾校园网，坑我钱财，毁我青春" +
                "\n(PS:我14届信工，用的技术一半以上都是老师教过的哦hhh)"
        );
    }
}
