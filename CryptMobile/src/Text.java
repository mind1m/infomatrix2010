
import java.util.Random;
import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import javax.microedition.io.*;
import javax.wireless.messaging.MessageConnection;
import javax.wireless.messaging.TextMessage;

public class Text extends MIDlet implements CommandListener {

    long p,q,n,f,e,d;
    boolean wg=false;
    public long QuickPow(long a, long w, long n)
        {
            //s=a^w  mod n; быстрое возведение a в степень w по модулю n
            long s = 1, v = w, c = a;
            if ((a <= 0) || (w < 0))
            {
                System.out.println("s = "+s+" v = "+v+", c = "+c);
                System.out.println("Error QuickPow");
            }
            while (v != 0)
            {
                if (v % 2 == 1)
                {
                    s = (s * c) % n;
                    v = (v - 1) / 2;
                }
                else v = v / 2;
                c = (c * c) % n;
            }
            return s;
        }


    public long generateSimple() {
        long p;


        p=r.nextInt(1000);
        //System.out.println(p);
        if (p<0) {
            p=-p;
        }
        if (p % 2==0) {
            p++;
        }
        boolean pr=false;
        while ( !pr) {
            pr=true;
            for (int i=3; i<p; i++) {
                if (p % i==0) {
                    pr=false;
                }
            }
            p=p+2;
            //System.out.println(p);
        }


        p=p-2;
        //System.out.println(p);
        return p;
    }

    TextBox SmsBox;
    ChoiceGroup wtd;
    TextField intext,outtext,ok,sk;
    Form frm;
    Display ds;
    Command doit, gen,send,lSend,exit;
    Random r;
    public void generateNumbers () {

        p=generateSimple();
        q=generateSimple();

        //p and q

        n=p*q;
        f=(p-1)*(q-1);
        e=3; // 2^2^n+1
        while (f % e==0) {
            e++;
        }

        //long k=r.nextInt()+1;
        long k=1;
        boolean b;
        b=false;
        while (!b) {
            if ((1+k*f) % e==0) {
                b=true;
            }
            k++;
        }
        k--;
        d=(1+k*f);
        d=d/e;

        ok.setString(Integer.toString((int) e)+" "+Integer.toString((int) n));
        sk.setString(Long.toString(d)+" "+Integer.toString((int) n));

        wg=true;


    }

    public void decrypt() {

       if (sk.getString()!=(Long.toString(d)+" "+Integer.toString((int) n))) {
            StringBuffer tb;
            tb= new StringBuffer(sk.getString());
            String ts ="";

            for (int i=0; i<tb.length(); i++) {
                if (tb.charAt(i)!=' ') {
                    ts+=tb.charAt(i);
                } else {
                    System.out.println(ts);
                    d=Long.parseLong(ts);

                    ts="";
                }

            }
            System.out.println(ts);
            n=Long.parseLong(ts);
            wg=true;
       }
       if (wg) {
            long h=0;
            String ts="";
            StringBuffer sb,ob;
            ob = new StringBuffer("");
            sb = new StringBuffer(intext.getString()+" ");
            for (int i=0;i<sb.length();i++) {
                if (sb.charAt(i)==' ') {
                    h=Long.parseLong(ts);
                    long h2=QuickPow(h, d, n);
                    ob.append((char)h2);
                    ts="";
                } else {
                    ts+=sb.charAt(i);
                }
            }

            outtext.setString(ob.toString());

       }

    }

    public void crypt() {


       if (wg) {
            long m=0;
            StringBuffer sb,ob;
            ob = new StringBuffer("");
            sb = new StringBuffer(intext.getString());
            for (int i=0;i<sb.length();i++) {
                m=sb.charAt(i);
                long h=QuickPow(m, e, n);
                ob.append(h);
                ob.append(" ");
            }
            ob.deleteCharAt(ob.length()-1);
            outtext.setString(ob.toString());

       }

    }

    void smsSend() {
        try {
            String addr = "sms://" + SmsBox.getString(); // Строка адреса
            MessageConnection conn = (MessageConnection) Connector.open(addr);
            TextMessage msg = (TextMessage) conn.newMessage(MessageConnection.TEXT_MESSAGE);
            msg.setPayloadText(outtext.getString()); // Ложим в обьект msg наше сообщение из getmes
            conn.send(msg); // Отправляем наше сообщение
            Alert a= new Alert("Sent", "Sent", null, AlertType.INFO);
            a.setTimeout(1000);
            ds.setCurrent(a);
        } catch(Exception e) {
            Alert a= new Alert("Error", "Sending problem", null, AlertType.ERROR);
            a.setTimeout(1000);
            System.out.println("trouble");
            ds.setCurrent(a);
        }
    }

    public void startApp() {


        ds = Display.getDisplay(this);
        frm = new Form("Text encrypting");
        intext = new TextField("Text:", "", 500, TextField.ANY);
        frm.append(intext);



        ok = new TextField("Public key:", "", 50, TextField.ANY);
        frm.append(ok);

        sk = new TextField("Private key:", "", 50, TextField.ANY);
        frm.append(sk);

        wtd = new ChoiceGroup("Action: ", List.POPUP);
        wtd.append("Encrypting", null);
        wtd.append("Decrypting", null);
        frm.append(wtd);

        outtext = new TextField("Result:", "", 5000, TextField.ANY);
        frm.append(outtext);

        frm.setCommandListener(this);
        doit = new Command("Do",Command.OK,1);
        frm.addCommand(doit);
        gen = new Command("Generate keys",Command.OK,2);
        frm.addCommand(gen);
        send=new Command("Send result", Command.OK, 3);
        frm.addCommand(send);
        exit = new Command("Back",Command.BACK,4);
        frm.addCommand(exit);
        ds.setCurrent(frm);
        r = new Random();

        lSend=new Command("Send", Command.OK, 1);
        SmsBox =new TextBox("Enter number:", "+", 18, TextField.PHONENUMBER);
        SmsBox.setCommandListener(this);
        SmsBox.addCommand(lSend);

    }

    public void pauseApp() {
    }

    public void destroyApp(boolean unconditional) {
        notifyDestroyed();
    }

    public void commandAction(Command c, Displayable d) {
        if (c==doit) {
           if (wtd.isSelected(0)) {

               crypt();
           } else {
               decrypt();
           }
        } else if (c==send) {
            if (outtext.getString().length() > 0) {
                ds.setCurrent(SmsBox);
            }
        } else if(c==lSend) {
            smsSend();
            ds.setCurrent(frm);
        } else if (c==exit) {
            destroyApp(false);
        } else {
            generateNumbers();
        }

    }
}
