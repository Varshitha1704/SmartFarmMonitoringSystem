import java.awt.Toolkit;
import java.lang.reflect.Method;

public class VoiceAlert {
    public static void speak(String message) {
        try {
            Class<?> managerClass = Class.forName("com.sun.speech.freetts.VoiceManager");
            Object manager = managerClass.getMethod("getInstance").invoke(null);
            Object voice = managerClass.getMethod("getVoice", String.class).invoke(manager, "kevin16");
            if (voice != null) {
                Method allocate = voice.getClass().getMethod("allocate");
                Method speak = voice.getClass().getMethod("speak", String.class);
                allocate.invoke(voice);
                speak.invoke(voice, message);
                return;
            }
        } catch (Exception ignored) {
            // FreeTTS is optional at runtime; the desktop beep is the fallback.
        }
        Toolkit.getDefaultToolkit().beep();
        System.out.println("[Voice alert] " + message);
    }
}
