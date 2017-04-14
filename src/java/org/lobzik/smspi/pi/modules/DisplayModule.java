/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lobzik.smspi.pi.modules;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import org.apache.log4j.Logger;
import org.lobzik.smspi.pi.AppData;
import org.lobzik.smspi.pi.event.Event;

/**
 *
 * @author lobzik
 */
import javax.imageio.ImageIO;

import org.apache.log4j.Appender;
import org.lobzik.home_sapiens.entity.Measurement;
import org.lobzik.home_sapiens.entity.Parameter;
import org.lobzik.smspi.pi.BoxCommonData;
import org.lobzik.smspi.pi.ConnJDBCAppender;
import org.lobzik.smspi.pi.MessageStatus;
import org.lobzik.smspi.pi.event.EventManager;
import org.lobzik.tools.Tools;
import org.lobzik.tools.db.mysql.DBSelect;
import org.lobzik.tools.db.mysql.DBTools;

public class DisplayModule implements Module {

    public final String MODULE_NAME = this.getClass().getSimpleName();
    private static DisplayModule instance = null;

    private static final String TMP_FILE = "i.tmp";
    private static final String IMG_FILE = "i.png";
    private static final String SYMLINK1 = "a.png";
    private static final String SYMLINK2 = "b.png";

    private static Logger log = null;
    private static final String PREFIX = "/usr/bin/sudo";
    private static final String FBI_COMMAND = "/usr/bin/fbi";
    private static final String LN_COMMAND = "/bin/ln";
    private FbiRunner fbiRunner = null;
    private static final Color DAY_FONT_COLOR = new Color(255, 255, 255);
    private static int rssi = -101;
    private static int balance = -1123;
    private static String operator = "";

    private DisplayModule() { //singleton
    }

    public static DisplayModule getInstance() {
        if (instance == null) {
            instance = new DisplayModule(); //lazy init
            log = Logger.getLogger(instance.MODULE_NAME);
            Appender appender = ConnJDBCAppender.getAppenderInstance(AppData.dataSource, instance.MODULE_NAME);
            log.addAppender(appender);
        }
        return instance;
    }

    @Override
    public String getModuleName() {
        return MODULE_NAME;
    }

    @Override
    public void start() {
        try {
            try {
                if (AppData.onPi) {//pi is sudoer
                    Tools.sysExec("sudo killall -9 fbi", new File("/"));
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }

            draw();
            EventManager.subscribeForEventType(this, Event.Type.TIMER_EVENT);
            EventManager.subscribeForEventType(this, Event.Type.SYSTEM_EVENT);
            EventManager.subscribeForEventType(this, Event.Type.BEHAVIOR_EVENT);
            EventManager.subscribeForEventType(this, Event.Type.SYSTEM_MODE_CHANGED);
            EventManager.subscribeForEventType(this, Event.Type.PARAMETER_UPDATED);

            fbiRunner = new FbiRunner();
            fbiRunner.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handleEvent(Event e) {
        switch (e.type) {
            case TIMER_EVENT:
            case SYSTEM_EVENT:
                if (e.name.equals("update_display")) {
                    draw();
                }
                if (e.name.equals("modem_mode_updated")) {
                    int newRSSI = Tools.parseInt(e.data.get("RSSI"), rssi);
                    if (newRSSI != rssi) {
                        rssi = newRSSI;
                        draw();
                    }

                }
                break;

            case SYSTEM_MODE_CHANGED:
                draw();
                break;
            case PARAMETER_UPDATED:
                try {
                    if (e.name.equals("Balance updated")) {
                        Measurement m = (Measurement) e.data.get("measurement");
                        int newBalance = m.getDoubleValue().intValue();
                        if (newBalance != balance) {
                            balance = newBalance;
                            draw();
                        }
                    }
                } catch (Exception eee) {
                }

                break;

        }

    }

    private void draw() {
        try {
            log.debug("Drawing img for screen");
            Graphics g = null;
            BufferedImage img = null;
            Parameter operP = AppData.parametersStorage.getParameterByAlias("MODEM_OPERATOR");

            if (AppData.measurementsCache.getLastMeasurement(operP) != null) {
                operator = AppData.measurementsCache.getLastMeasurement(operP).toStringValue();
            }

            Parameter balP = AppData.parametersStorage.getParameterByAlias("MODEM_BALANCE");

            if (AppData.measurementsCache.getLastMeasurement(balP) != null) {
                balance = AppData.measurementsCache.getLastMeasurement(balP).getDoubleValue().intValue();
            }

            img = ImageIO.read(new File(AppData.getGraphicsWorkDir().getAbsolutePath() + File.separator + "background_day.jpg"));
            g = img.getGraphics();
            Graphics2D g2d = (Graphics2D) g;
            //int rssi = -101;//сигнал сети, дБ. <100 нет фишек, 100<rssi<90 одна фишка, 90<rssi<80 две фишки, 80<rssi<70 три фишки, >70 четыре фишки
            String modemMode = "4G";//Режим сети. приедет от модема

            Color fontColor = DAY_FONT_COLOR;

            g.setColor(fontColor);
            g.setFont(new Font("Roboto Regular", Font.BOLD, 30));
            g.drawString(Tools.getFormatedDate(new Date(), "HH:mm"), 210, 35);

            //Modem
            g.setColor(fontColor);

            g.setFont(new Font("Roboto Regular", Font.BOLD, 30));
            g.drawString(operator, 324, 35);

            //rssi
            g.setColor(new Color(255, 255, 255, 200));
            g.fillRect(250, 118, 10, 12);
            g.fillRect(262, 106, 10, 24);
            g.fillRect(274, 94, 10, 36);
            g.fillRect(286, 82, 10, 48);

            //int rssi = -75;//сигнал сети, дБ. <100 нет фишек, 100<rssi<90 одна фишка, 90<rssi<80 две фишки, 80<rssi<70 три фишки, >70 четыре фишки
            if (rssi <= -100) {
                g2d.setColor(new Color(255, 0, 0));
                g2d.setStroke(new BasicStroke(5.0f));
                g2d.drawOval(250, 80, 50, 50);
                g2d.drawLine(260, 120, 290, 90);
            }

            g.setColor(fontColor);
            if (rssi > -100) {
                g.fillRect(250, 118, 10, 12);
            }
            if (rssi > -90) {
                g.fillRect(262, 106, 10, 24);
            }
            if (rssi > -80) {
                g.fillRect(274, 94, 10, 36);
            }
            if (rssi > -70) {
                g.fillRect(286, 82, 10, 48);
            }

            g.setFont(new Font("Roboto Regular", Font.BOLD, 60));
            if (balance == -1123) {
                g.drawString("Н/Д", 250, 238);
            } else {
                g.drawString(balance + "р", 250, 238);
            }

            if (rssi > -100) {
                g.drawString(rssi + "", 303, 130);
                g.setFont(new Font("Roboto Regular", Font.BOLD, 40));
                g.drawString("dB", 407, 130);

            }

            try (Connection conn = DBTools.openConnection(BoxCommonData.dataSourceName)) {
                Long msgSent = DBSelect.getCount("select count(*) as cnt from sms_outbox where status = " + MessageStatus.STATUS_SENT, "cnt", null, conn);
                Long msgReceived = DBSelect.getCount("select count(*) as cnt from sms_inbox", "cnt", null, conn);
                Long msgErrs = DBSelect.getCount("select count(*) as cnt from sms_outbox where status in (" + MessageStatus.STATUS_ERROR_SENDING + "," + MessageStatus.STATUS_ERROR_TOO_OLD + "," + MessageStatus.STATUS_ERROR_ATTEMPTS_EXCEEDED + ")", "cnt", null, conn);
                g.setColor(new Color(255, 255, 255, 200));
                g.setFont(new Font("Roboto Regular", Font.BOLD, 30));
                g.drawString("Отправлено", 21, 90);
                g.drawString("Принято", 21, 188);
                g.drawString("Баланс", 250, 188);
                g.drawString("Ошибок", 21, 230);

                g.setColor(fontColor);

                g.drawString("" + msgReceived, 160, 188);
                g.drawString("" + msgErrs, 160, 230);

                g.setFont(new Font("Roboto Regular", Font.BOLD, 65));
                g.drawString("" + msgSent, 21, 148);

            } catch (Exception e) {
                e.printStackTrace();
            }

            File file = new File(AppData.getGraphicsWorkDir().getAbsolutePath() + File.separator + TMP_FILE);
            FileOutputStream fos = new FileOutputStream(file);
            java.nio.channels.FileLock lock = fos.getChannel().lock();
            try {
                ImageIO.write(img, "png", fos);
                fos.flush();

            } finally {
                lock.release();
            }
            fos.close();
            Path src = Paths.get(AppData.getGraphicsWorkDir().getAbsolutePath() + File.separator + TMP_FILE);
            Path dst = Paths.get(AppData.getGraphicsWorkDir().getAbsolutePath() + File.separator + IMG_FILE);

            Files.move(src, dst, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);

        } catch (Exception e) {
            log.error("Error " + e.getMessage());
        }

    }

    public static void finish() {
        try {
            instance.fbiRunner.finish();
            if (AppData.onPi) {//pi is sudoer
                Tools.sysExec("sudo killall -9 fbi", new File("/"));
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    /*
    public static Image transcodeSVGDocument( URL url, int x, int y ){
    // Create a PNG transcoder.
    Transcoder t = new PNGTranscoder();

    // Set the transcoding hints.
    t.addTranscodingHint( PNGTranscoder.KEY_WIDTH,  new Float(x) );
    t.addTranscodingHint( PNGTranscoder.KEY_HEIGHT, new Float(y) );

    // Create the transcoder input.
    TranscoderInput input = new TranscoderInput( url.toString() );

    ByteArrayOutputStream ostream = null;
    try {
        // Create the transcoder output.
        ostream = new ByteArrayOutputStream();
        TranscoderOutput output = new TranscoderOutput( ostream );

        // Save the image.
        t.transcode( input, output );

        // Flush and close the stream.
        ostream.flush();
        ostream.close();
    } catch( Exception ex ){
        ex.printStackTrace();
    }

    // Convert the byte stream into an image.
    byte[] imgData = ostream.toByteArray();
    Image img = Toolkit.getDefaultToolkit().createImage( imgData );

    // Return the newly rendered image.
    return img;
}
     */
    private static BufferedImage createResizedCopy(Image originalImage, int scaledWidth, int scaledHeight, boolean preserveAlpha) {
        System.out.println("resizing...");
        int imageType = preserveAlpha ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
        BufferedImage scaledBI = new BufferedImage(scaledWidth, scaledHeight, imageType);
        Graphics2D g = scaledBI.createGraphics();
        if (preserveAlpha) {
            g.setComposite(AlphaComposite.Src);
        }
        g.drawImage(originalImage, 0, 0, scaledWidth, scaledHeight, null);
        g.dispose();
        return scaledBI;
    }

    public static class FbiRunner extends Thread {

        private OutputStreamWriter osr = null;
        private Process process = null;
        private boolean run = true;

        public void finish() {
            log.info("Exiting process");
            try {
                run = false;
                osr.write("q");
                osr.flush();
                process.destroyForcibly();
            } catch (Exception e) {
            }
        }

        @Override
        public void run() {
            try {
                //creating file
                List<String> command = new LinkedList();
                log.debug("Started, drawing image");
                instance.draw();
                String[] env = {"aaa=bbb", "ccc=ddd"};
                String imgFile = AppData.getGraphicsWorkDir().getAbsolutePath() + File.separator + IMG_FILE;
                String symLink1 = AppData.getGraphicsWorkDir().getAbsolutePath() + File.separator + SYMLINK1;
                String symLink2 = AppData.getGraphicsWorkDir().getAbsolutePath() + File.separator + SYMLINK2;
                command.clear();
                command.add(LN_COMMAND);
                command.add("-sf");
                command.add(imgFile);
                command.add(symLink1);

                log.debug("Creating symlink1");

                File workdir = AppData.getGraphicsWorkDir();
                Runtime runtime = Runtime.getRuntime();
                process = runtime.exec(command.toArray(new String[command.size()]), env, workdir);
                process.waitFor();
                int exitValue = process.exitValue();
                if (exitValue != 0) {
                    log.error("error creating symlink1, exit status= " + exitValue);
                    return;
                }

                command.clear();
                command.add(LN_COMMAND);
                command.add("-sf");
                command.add(imgFile);
                command.add(symLink2);

                log.debug("Creating symlink2");

                process = runtime.exec(command.toArray(new String[command.size()]), env, workdir);
                process.waitFor();
                exitValue = process.exitValue();
                if (exitValue != 0) {
                    log.error("error creating symlink2, exit status= " + exitValue);
                    return;
                }

                command.clear();
                command.add(PREFIX);
                command.add(FBI_COMMAND);
                command.add(imgFile);
                command.add(symLink1);
                command.add(symLink2);
                command.add("-a");
                command.add("-noverbose");
                command.add("-t");
                command.add("1");
                command.add("-cachemem");
                command.add("0");
                command.add("-T");
                command.add("1");

                log.info("Running FBI");
                while (run) {
                    try {
                        exitValue = 0;
                        process = runtime.exec(command.toArray(new String[command.size()]), env, workdir);
                        //osr = new OutputStreamWriter(process.getOutputStream());
                        process.waitFor();
                        log.info("FBI exited");
                        exitValue = process.exitValue();

                    } catch (Throwable e) {
                        log.error(e.getMessage());
                    }
                    if (exitValue != 0) {
                        log.debug("error in FBI, exit status= " + exitValue);
                        Thread.sleep(10000);
                    } else {
                        break;
                    }
                }

            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }
    }

}
