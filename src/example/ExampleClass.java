package example;

import java.net.SocketException;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.net.HttpCookie;
import java.net.DatagramSocket;

public class ExampleClass implements Runnable {

    private List<Integer> ints;
    private Map<String, Integer> userAgeMap;
    private HttpCookie cookie;
    private DatagramSocket socket;


    public ExampleClass() {
        this.ints = new ArrayList<>();
        this.cookie = new HttpCookie("mycookie", "pwd");
        try {
            this.socket = new DatagramSocket();
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }

    public void addInt(Integer myint) {
        ints.add(myint);
    }

    public Integer getInt(int index) throws IndexOutOfBoundsException {
        return ints.get(index);
    }

    @Override
    public void run() {
        System.out.println("Running example...");
    }

    private static class InnerClass {
        private String innerField;
    }
}

