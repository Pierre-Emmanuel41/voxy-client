package fr.pederobien.voxy.client;

import fr.pederobien.utils.event.Logger;
import fr.pederobien.voxy.client.impl.Voxy;
import fr.pederobien.voxy.client.interfaces.IVoxyClient;

import java.util.function.Consumer;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) {
        Logger.instance().newLine(true).timeStamp(true).colorized(true).debug(true).register();

        IVoxyClient client = Voxy.createClient("BeKoolMan", "127.0.0.1", 12345);
        Consumer<Boolean> callback = success -> Logger.info(success ? "The client is connected" : "Error happened when trying to connect");
        client.connect(callback);

        try {
            Thread.sleep(100000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
