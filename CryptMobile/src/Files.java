/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Random;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.midlet.*;
import javax.microedition.io.*;
import javax.microedition.io.file.FileConnection;
import javax.microedition.io.file.FileSystemRegistry;
import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.TextField;


/**
 * @author Admin
 */
public class Files extends MIDlet implements CommandListener {
    Form frm,waitform;
    Display ds;
    TextField filename,pass,ok,sk;
    Command doit, gen,send,lSend,exit;
    Alert a,b;
    ChoiceGroup wtd;
    Random r;
    long p,q,n,f,e,d;
    boolean wg=false;


    public long QuickPow(long a, long w, long n)
        {
            //s=a^w  mod n; быстрое возведение a в степень w по модулю n
            long s = 1, v = w, c = a;
            if ((a < 0) || (w < 0))
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
        long res;


        res=r.nextInt(1000);
        //System.out.println(p);
        if (res<0) {
            res=-res;
        }
        if (res % 2==0) {
            res++;
        }
        boolean pr=false;
        while ( !pr) {
            pr=true;
            for (int i=3; i<res; i++) {
                if (res % i==0) {
                    pr=false;
                }
            }
            res=res+2;
            //System.out.println(p);
        }


        res=res-2;
        //System.out.println(p);
        return res;
    }


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


    public void crypt() throws IOException {

       FileConnection fc = null;
       if (wg) {
            long m=0;


            fc = (FileConnection) Connector.open("file:///"+filename.getString());
            byte[] b = new byte[(int)fc.fileSize()];
            InputStream is= fc.openInputStream();
            int l=is.read(b); //size

            is.close();

            DataOutputStream os = fc.openDataOutputStream();
            for (int i=0;i<l;i++) {

                long h=QuickPow(b[i], e, n);
                os.writeLong(h);
            }
            os.close();


       }
       fc.close();

    }



    public void decrypt() {
       FileConnection fc = null;
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
            /*String ts="";
            StringBuffer sb,ob;
            ob = new StringBuffer("");
            sb = new StringBuffer(intext.getString()+" "); */
            try {
                fc = (FileConnection) Connector.open("file:///"+filename.getString());
                DataInputStream is = fc.openDataInputStream();

                StringBuffer sb= new StringBuffer("");
                long l=fc.fileSize()/8;
                for (int i=0; i<l; i++) {
                    h=is.readLong();
                    long h2=QuickPow(h, d, n);
                    sb.append((char)h2);

                }


                is.close();
                fc.delete();
                fc.create();
                OutputStream os = fc.openOutputStream();
                for (int i=0; i<sb.length(); i++) {
                    os.write(sb.charAt(i));
                }
                os.close();
                fc.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }




       }

    }



    public void startApp() {
        ds=Display.getDisplay(this);
        frm=new Form("File crypting");
        Enumeration e=FileSystemRegistry.listRoots();
        while (e.hasMoreElements())
        {

            frm.append((String)e.nextElement());
        }
        filename= new TextField("File path:", "E:/test.png", 255, TextField.ANY);
        frm.append(filename);

         ok = new TextField("Public key:", "", 50, TextField.ANY);
        frm.append(ok);

        sk = new TextField("Private key:", "", 50, TextField.ANY);
        frm.append(sk);

        wtd = new ChoiceGroup("Action: ", List.POPUP);
        wtd.append("Encrypting", null);
        wtd.append("Decrypting", null);

        frm.append(wtd);

        doit = new Command("Do it", Command.OK, 0);
        frm.setCommandListener(this);
        gen = new Command("Generate keys",Command.OK,2);
        frm.addCommand(gen);
        exit = new Command("Back",Command.BACK,4);
        frm.addCommand(exit);
        frm.addCommand(doit);
        ds.setCurrent(frm);
        //doStuff();
        r=new Random();

        waitform= new Form("Ожидание");
        waitform.append("Подождите пожалуйста...");

    }

    public void pauseApp() {
    }

    public void destroyApp(boolean unconditional) {
        notifyDestroyed();
    }

    public void commandAction(Command c, Displayable d) {
        if (c==doit) {
            b= new Alert("Подождите пожалуйста...", "Подождите пожалуйста...", null, AlertType.INFO);
            b.setTimeout(Alert.FOREVER);
            ds.setCurrent(b);

            if (wtd.isSelected(0)) {

                try {
                    crypt();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }

            } else {

                decrypt();
            }

            a= new Alert("Done", "Done", null, AlertType.INFO);
            a.setTimeout(1000);
            ds.setCurrent(a);


        } else if(c==exit) {
                destroyApp(false);
        } else if (c==gen) {
                generateNumbers();
        }
    }
}