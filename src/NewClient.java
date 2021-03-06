/*
import activity.ClientPacket;
import security.User;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import people.Donut;
import people.Fool;
import people.Human;
import rocket.room.Room;
import rocket.room.Type;

import java.io.Console;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.PortUnreachableException;
import java.nio.CharBuffer;
import java.util.Scanner;
import static security.Serializer.*;
import static security.MD2Hasher.*;

public class NewClient {
    private static final Room temporyRoom = new Room(Type.FOODSTORAGE, "Бездна");
    public static void main(String[] args) throws IOException {
        System.out.println("Welcome to Client side\n" +
                "to register new user type \"reg\" <your mail>\n" +
                "to use your account use \"sign in\" command:\n" +
                "to exit your account type \"sign out\"\n" +
                "to exit program use \"exit\"");
        DatagramSocket socket = new DatagramSocket();
        InetAddress address = InetAddress.getByName("localhost");
        socket.connect(address, 8989);
        Scanner scanner = new Scanner(System.in);
        try {
            while (true) {
                try {
                    System.out.print("->");
                    String line = scanner.nextLine();

                    if (line.equalsIgnoreCase("exit")) break;
                    if (line.isEmpty()) continue;
                    if (line.split(" ", 2)[0].equals("reg")){
                        send(line.split(" ",2)[1], null, null, socket);
                        receive(socket);
                    }else if(line.equals("sign in")) {
                        Console console = System.console();
                        String login = console.readLine("Login:");
                        String password = hashString(new String(console.readPassword("Password:")));
                        send(null,null, new User(login,password), socket);
                        if(!receive(socket).equals("Welcome back "+login)){
                            continue;
                        }
                        line = "help";
                        while(true) {
                            String commandWord = line.split(" ")[0];
                            try {
                                if (commandWord.equals("import")) {
                                    line = "load " + readFile(line.split(" ", 2)[1]);
                                }
                            } catch (IOException e) {
                                System.out.println("File " + line.split(" ", 2)[1] + " does not exist");
                                continue;
                            }
                            Human human;
                            if (commandWord.equals("add") || commandWord.equals("remove_lower") || commandWord.equals("add_if_max") || commandWord.equals("remove")) {
                                try {
                                    human = readJSON(line.split(" ", 2)[1], login);
                                    send(commandWord, human, new User(login, password), socket);
                                } catch (ParseException e) {
                                    System.out.println("Wrong format!\n->");
                                    line = scanner.nextLine();
                                    continue;
                                } catch (NullPointerException e) {
                                    System.out.println("wrong format!\n->");
                                    line = scanner.nextLine();
                                    continue;
                                }
                            } else if(line.equals("sign out")) {
                                System.out.println("Goodbye!");
                                break;
                            } else {
                                send(line, null, new User(login, password), socket);
                            }
                            receive(socket);
                            System.out.print(login + "->");
                            line = scanner.nextLine();
                        }
                    }else {
                        System.out.println("Input error. Please try again or see 'help'.");
                    }
                }catch (ArrayIndexOutOfBoundsException e){
                    System.out.println("Wrong format!");
                }
            }
            socket.close();
        }catch (PortUnreachableException e){
            printMeme();
        }
    }
    private static String readFile(String file) throws IOException {
        CharBuffer buffer = CharBuffer.allocate(65000);
        FileReader reader = new FileReader(file);
        reader.read(buffer);
        buffer.flip();
        int limits = buffer.limit();
        char[] chars = new char[limits];
        buffer.get(chars, 0, limits);
        String msg = new String(chars);
        reader.close();
        return msg;
    }
    private static Human readJSON(String string, String username) throws ParseException, NullPointerException {
        Human human;
        JSONObject jo = (JSONObject) new JSONParser().parse(string);
        String name;
        if(jo.get("name") instanceof String) {
            name = (String) jo.get("name");
        }else{
            throw new ParseException(0);
        }
        String foodName;
        try {
            if(jo.get("foodName") instanceof String) {
                foodName = (String) jo.get("foodName");
            }else{
                throw new ParseException(0);
            }
        }catch (NullPointerException e){
            foodName = "";
        }
        int timeUntilHunger;
        if(jo.get("timeUntilHunger") instanceof Long) {
            timeUntilHunger = ((Long) jo.get("timeUntilHunger")).intValue();
        }else{
            throw new ParseException(0);
        }
        int thumbLength;
        try {
            if(jo.get("thumbLength") instanceof Long) {
                thumbLength = ((Long)jo.get("thumbLength")).intValue();
            }else {
                throw new ParseException(0);
            }
        }catch (NullPointerException e){
            thumbLength = 0;
        }
        if (timeUntilHunger < 1) throw new ParseException(1);
        if (name.isEmpty()){
            human = new Human(timeUntilHunger, username, temporyRoom);
        }else if (thumbLength > 0){
            if (!foodName.isEmpty()) {
                human = new Fool(name, timeUntilHunger, temporyRoom, foodName, thumbLength, username);
            }else{
                human = new  Fool(name, timeUntilHunger, temporyRoom, thumbLength, username);
            }
        }else if (!foodName.isEmpty()){
            human = new Donut(name, timeUntilHunger, temporyRoom, foodName, username);
        }else{
            human = new Human(name, timeUntilHunger, username, temporyRoom);
        }
        return human;
    }

    private static void send(String commandWord, Human human, User user, DatagramSocket socket) throws IOException {
        ClientPacket clientPacket = new ClientPacket(commandWord, human, user);
        byte[] buf = serialize(clientPacket);
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        socket.send(packet);
    }

    private static String receive(DatagramSocket socket) throws IOException {
        String received;
        byte[] buffer = new byte[65000];
        DatagramPacket packet1 = new DatagramPacket(buffer, buffer.length);
        socket.receive(packet1);
        received = new String(packet1.getData(), 0, packet1.getLength());
        System.out.println(received);
        return received;
    }
    private static void printMeme(){
        System.out.println("2Xi2s:rsiiiiiiSiSiSSSSSSSSSSSSSSSSSSSSSSSSSSSSSS2sr;SsSi5SSsisSSS59ABBBBBBHG255555523&HBBHGX223&&32X9X9&GHAGBG\n" +
                "32S5r;siiiiiiiSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSS22X5;:rrrSSsrr;;:;r52hABBBBBHG3555555223ABMBBBA322XX2XXh22&&&HMMG\n" +
                "SS5S:;siiiiiSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSssSSi,,;rS5i5Srir;;r::;S9ABBBBBA92555552223&BMMMMBHhX2222XG&X9&BBHG9\n" +
                "i:,,,riiiiiiiiSSSSSSSSSSSSSSSSSSSSSSSSSS;:;i5ii;;;rS325ss:;,::::r;S&HBBBAhX5522222229ABMMMMMMB&32X2XhAhSii225S\n" +
                "XSissiiiiiiiiSSSSSSSSSSSSSSSSSSSSSSSSS5s;:r5S5;rsrSissrr:rr:,;:;:;;sHHHAhX52299222229ABMMMMMMMBA9X223&Hh39hh93\n" +
                "XSiiiiiiiiSSSSSSSSSSSSSSSS5SSSSSSSSSS55r.;rsS;:i:;55rSis;ss:,,:,,:,::HAhX252h&G3222X9ABMMMMMM#MMBGX22X3339h93X\n" +
                "2SiiiiiiiiiiiiSiiiiisssrrsrsssrrrssiiS;.:;iisrrissss25539Xisr::....,;:925229AH&3222X9ABMMMMMMMMMMB&3222X33993X\n" +
                "XSSisiiiiiiisrrrrr;;;;rrrrrrrrrrrrrr;r,,rSr;r5939hh33Xs3hh25ir;.,.. ,:r5529ABBA322229ABMMMMMMMMBH&9X222X3399XX\n" +
                "2Siiiiissrr;::;;:;;:;rrr;rrrrrrsrrrrr;,rrr2SX&AMM#MH&G253hG3isr::   . ;529&HBB&X22229ABMMMMMBBBAh3X22X3X33993X\n" +
                "2SSisr;;;;;;;rr;;;r;r;;;;srrrrrsrrrrr:,r523hHBM####MH2G523999X5r:.  .i;:GAHBBB&X222X9ABMMMMBBH&3XX222X333399X2\n" +
                "2SSs::;;;;;;;r;;;;;;r;;r;;;rr;;:;;r;r;,sS2h&HBBMMM#BHH3G95393XXSS,.,,rsi:HBBBH&X222X9ABMMBBH&h3XX33XXX2X33933X\n" +
                "2Sis,:;:;;s;;;:;;;:;:::;;::::::::::::r;5sXhGAHHBM##MBBH939iS93X32:..,:s,.hBBBB&3222X9ABMBBHG3XX39ABBA&&933hh3X\n" +
                "2Sii,::;;:;;;:;:::::;;;;;;;:;:;;;;;;;;:Si9G&HBBMMMMMMBA935XXs5532;,.,r:rr;BBBB&X22229ABBBHh3XX3GABMMBBBGhhGG93\n" +
                "2SSi::r;;:::::;;:::;;;;;;rrrr;r;r;;;;;;ir3GAAHHHHAA&9255s,sriS93XS,.::S:r;HBBH&X52229ABHAhXXX9&HBMMMBMBA&&A&h3\n" +
                "XSSs::::,::::::;;r;;;rssrrrrrrrrrrrrr:;S;5239X233GAG5i2XGhh3G&&G3Sr::r:;:r&BBHGX22223&AG9X2X9GHBMMMBBBBAAAHAG9\n" +
                "2Sii:::::::;;;;;;r;;;;;;;;;;;;;;;;;;rr:i;,5ir2sh9hHH99&GGGAHHA&hXSr;;SrS;rXHBH&X2222399XXXXXX39GAHBBBBBHHHBA&h\n" +
                "2Sis::,,::::;;;;::::;;;rrrr;;;r;rrrrrs,:i:sh9hhhGAHHhhGAHMMMBAG32isrriii;;s5HHGX222222222X3XXXXXX339hGhAHHHH&h\n" +
                "2iSs,::::::;:::;;;;;rrr;;;;;;r;;;;;;rS:,is:9&AAHH&HBGGhh&AHHAGhX22Ss:ii;r::2BHhX222222222X9X33hGh3X22XXAAAHH&9\n" +
                "2Sis:;:::;::::;:;;;;;;;:::,,,:::::;:;2rssrsiGAHHA&&MA&9h2G&hhh3XXXSS;5sr5::93AGX52222222X399GAHBBHA&hXX&&G&&h9\n" +
                "2Sis:::::;;;;:;:::,.   ....,,,,:,,,,,2;5ssrsSG&&GG&A&XrrSX3X32G3X2isrsrrS:i29Ah2222222X529hhAHBBBBH&h9XhhhGGh3\n" +
                "2iis::,::;;:::,.            ,,rA&&G5:2;5r;rS,r99Xh3Sr;r;i55ss53h22s;.sis2;;ri    552225S3Gh9GHHHA&h3XXXhh9hh93\n" +
                "2iis,:::;::,,         ...,,3BHBBBHAh&G93rrr,r X3X5iiiS25XrS3hGXh25r::rs;;. ,.    rs22255993XX&Ah22222XX993h993\n" +
                "2iis::;:,,,        ..,,,,,:AAHBHHA9G3hGi2sS:r  59ii9S999GAGhhhh22S::;,ir., .    :, . ,2i2X2SS2X5iS52X3h333993X\n" +
                "2iis::,..       ...,,....,X&&&Xr;2X2sXih29&i;, ,,5S9hGGGh3339G32i;..:.:..  ..        ::i;rir;;rriS23G&H93X333X\n" +
                "2iis,,.       .,,,.....,::&AhS:r;;r,,,,sr2hSs:,,;rr23XXXhhGGh95s,,,:, ,.  , .     ,.;;sr::;;;;;rrrXAABB93X3332\n" +
                "2iis,.      ...,. .,,,.,:G&&3rrsssr,,.:rr392S:.,:s:i23Xhh3h32Sr.,:;rr: ., ,.     ,.rr2s;;;;:::::;:;rrrsXXX3332\n" +
                "5isr. .     .....,,....,,&A&3Xsrrsr;,:;:ih325::.,,,,ri52555Ss,,::;rsii;,i2.   , ::;rSs;;rr;;;;:::::::;;;;r;i32\n" +
                "2isr .      ....,,..,..i;&HAhSsiiS59h2sG&GG3;::.. , ...,;.   ,::;rSi,.2,  ,;,: .irsis:ssrrr;;:::;:;;:::::;;rr;\n" +
                "2isr      .....,,..,,.,2iAHAGXii5XhA&A&AA&9;:,.. , ,:;:,i..   ..rs, .:r2;:sr  rsrSisr;ssrrsr;;:;:;r;:::;;::;;r\n" +
                "  .;     ......,...,...:XAHH&9Si2hGABH&hhXr.    ..  rr;;:h,; ,,, ;:;rs53::;r,,r2i5sr;:ssrrrr;;;r.rsr;::;;:::;;\n" +
                "       ....... ........,5&HH&&XS2&AAA&9i5;.  .  .,. :r,;:,,,.;::,rrrss9;:;:;SsiSrsrr:;ssrssr;;;s,;ss;;;;:;;;;:\n" +
                "       ...........,....;srAH&&9S2hA&G95;,.  ., .:., ,,,:;r;;S:rsssrsr;:.,::;ssSrrssr:;ss;srrr;;s,:rsr;;;;:::rr\n" +
                "      ............ ....s;ii&&hG233&h35; .....  .. .. ,,:;;i;;;5@Biss;:;;sisisir;srs:,rsrrsrrrr;S:,sir;;;;;:;S5\n" +
                "        .........,,..,.r;riiiisS2i5is;; ,:  .  ..  , . ::rir;Ss;issr:,:ssSSiss;ssrr,,;srrsrssrrSr.is;rr:;r:;SS\n" +
                "       ...............Srssrsr5::;::,,::. .           ..:;ris;r:sssr::::i5iSiirrirs;::rsrrssssssSi is;sr;r;ssSs\n" +
                "        ...........,.:Srssrrr;:;r.,...  .  .       . .:,;rsiss;isir:,S;i2iiiirirss;,:rsssisiiisS5.S:ri;rrrS2ss\n" +
                "         .......,...,Si;r,;;9X:,,,:,.   ,          : .::;rr5s;ii3A;:,5;S5siississr;::rssSiisiSiSS,r,sSrr;22Si;\n" +
                "         .....,,....:r2s;5rr;r;..      .,.      .  :, :;;;iSsSr.rs,,.5s2SiSiririsrr::riiSSsiiiSSi,.:r5s;SSSis.\n" +
                "  s        .........ri5Srr:i ::,,..             .  :;.:;:;isSsr,rr,r i;ssissrsrs;r;:,;siSsrrssiss: ,;i;ii5is;,\n" +
                "\n" +
                "█──█─███───██─█────███─████---████─████─████─████─███─████---███─████─█──█---████──███─████─███─█\n" +
                "█──█─█────█─█─█──────█─█──█---█──█─█──█─█──█─█──█──█──█──█---─█──█──█─█─█─---█──██───█─█──█──█──█───\n" +
                "████─███─█──█─████─███─████---█──█─████─█──█─█─────█──█──█---─█──████─██──---████──███─████──█──████\n" +
                "█──█─█───█──█─█──█───█──█─█---█──█─█────█──█─█──█──█──█──█---─█──█──█─█─█─---█──██───█──█─█──█──█──█\n" +
                "█──█─███─█──█─████─███──█─█---█──█─█────████─████──█──████---─█──█──█─█──█---████──███──█─█──█──████\n" +
                "\n" +
                "---█──█---████──█───█─█──█───██─█──██──█──█─█──█─███─█───---████─███─████─████──███─████\n" +
                "---█──█---█──██─█───█─█─█───█─█─█─█──█─█──█─█──█──█──█───---█──█─█───█──█─█──██─█───█──█\n" +
                "---█─██---████──███─█─██───█──█─████─█─████─█─██──█──████---█────███─████─████──███─████\n" +
                "---██─█---█──██─█─█─█─█─█──█──█─█─█──█────█─██─█──█──█──█---█──█─█───█────█──██─█───█\n" +
                "---█──█---████──███─█─█──█─█──█─█──██─────█─█──█──█──████---████─███─█────████──███─█\n"+
                "Power the server!");
    }
}
*/
