//231RDB102 Jurģis Pāvulītis
//231RDB093 Patriks Grančarovs
//231RDB410 Džastins Zicāns
//231RDB282 Mārtiņš Zuments

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Scanner;
import java.util.Objects;
import java.io.File;

class Node implements Comparable < Node > {
    char data;
    int freq;
    Node left, right;

    Node(char data, int freq) {
        this.data = data;
        this.freq = freq;
        left = right = null;
    }

    public int compareTo(Node other) {
        return this.freq - other.freq;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Node other = (Node) obj;
        return data == other.data && freq == other.freq;
    }

    @Override
    public int hashCode() {
        return Objects.hash(data, freq);
    }
}

public class Main {
    private static Map < Character, String > hafMap;
    static Map < Character, Integer > frequencies = new HashMap < > ();
    public static void main(String[] args) {
        System.out.println("Enter command: \n" +
            "comp <file name> - compress file\n" +
            "decomp <file name> - decompress file\n" +
            "size <file name> - get file size\n" +
            "equal - check if files are equal\n" +
            "about - show authors\n" +
            "exit - exit program");
        Scanner sc = new Scanner(System.in);
        String cmd = "";
        while (!cmd.equals("exit")) {
            cmd = sc.nextLine();
            if (cmd.startsWith("comp ")) {
                String fileName = cmd.substring(5);
                compress(fileName);
            } else if (cmd.startsWith("decomp ")) {
                String fileName = cmd.substring(7);
                decompress(fileName);
            } else if (cmd.startsWith("size ")) {
                String fileName = cmd.substring(5);
                File file = new File(fileName);
                System.out.println("File size: " + file.length() + " bytes");
            } else if (cmd.equals("equal")) {
                System.out.print("first file name: ");
                String fileName1 = sc.nextLine();
                System.out.print("second file name: ");
                String fileName2 = sc.nextLine();
                try (BufferedInputStream reader1 = new BufferedInputStream(new FileInputStream(fileName1)); BufferedInputStream reader2 = new BufferedInputStream(new FileInputStream(fileName2))) {
                    int ch1, ch2;
                    boolean equal = true;
                    while ((ch1 = reader1.read()) != -1 && (ch2 = reader2.read()) != -1) {
                        if (ch1 != ch2) {
                            equal = false;
                            break;
                        }
                    }
                    if (equal && reader1.read() == -1 && reader2.read() == -1) {
                        System.out.println("true");
                    } else {
                        System.out.println("false");
                    }
                } catch (IOException e) {
                    System.out.println("error");
            }
            } else if (cmd.equals("about")) {
                System.out.println("231RDB102 Jurģis Pāvulītis\n" +
                "231RDB093 Patriks Grančarovs\n" +
                "231RDB410 Džastins Zicāns\n" +
                "231RDB282 Mārtiņš Zuments");
            } else if (!cmd.equals("exit")) {
                System.out.println("Wrong command");
            }
        }
        sc.close();
    }

    //================================================    

    public static void compress(String fileName) {
        try (BufferedReader br = new BufferedReader(new FileReader(fileName));
        BufferedOutputStream writer = new BufferedOutputStream(new FileOutputStream("comp_" + fileName))) {

            frequencies = new HashMap < > ();
            StringBuilder content = new StringBuilder();
            int ch;
            while ((ch = br.read()) != -1) {
                char c = (char) ch;
                content.append(c);
                frequencies.put(c, frequencies.getOrDefault(c, 0) + 1);
            }

            PriorityQueue < Node > prio = new PriorityQueue < > ();
            for (Map.Entry < Character, Integer > entry: frequencies.entrySet()) {
                prio.add(new Node(entry.getKey(), entry.getValue()));
            }

            while (prio.size() > 1) {
                Node left = prio.poll();
                Node right = prio.poll();
                Node parent = new Node('\0', left.freq + right.freq);
                parent.left = left;
                parent.right = right;
                prio.add(parent);
            }

            Node root = prio.peek();
            hafMap = new HashMap < > ();
            generateCodes(root, "");

            BitOutputStream out = new BitOutputStream(writer);
            for (char c: content.toString().toCharArray()) {
                writeCode(out, hafMap.get(c));
            }
            out.close();

            System.out.println("File compressed.");
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    //================================================

    private static void generateCodes(Node root, String code) {
        if (root == null) return;
        if (root.left == null && root.right == null) {
            hafMap.put(root.data, code);
        }
        generateCodes(root.left, code + "0");
        generateCodes(root.right, code + "1");
    }

    //================================================

    private static void writeCode(BitOutputStream out, String code) throws IOException {
        for (char c: code.toCharArray()) {
            out.writeBit(c == '1' ? 1 : 0);
        }
    }

    //================================================

    public static void decompress(String fileName) {
        try (BufferedInputStream reader = new BufferedInputStream(new FileInputStream(fileName));
        BufferedWriter writer = new BufferedWriter(new FileWriter("decomp_" + fileName.substring(5)))) {

            BitInputStream in = new BitInputStream(reader);
            StringBuilder content = new StringBuilder();
            StringBuilder currentCode = new StringBuilder();
            while (true) {
                int bit = in.readBit();
                if (bit == -1) {
                    break;
                }
                currentCode.append(bit);
                Character decodedChar = getCharacter(currentCode.toString());
                if (decodedChar != null) {
                    content.append(decodedChar);
                    currentCode = new StringBuilder();
                }
            }

            writer.write(content.toString());

            System.out.println("File decompressed.");
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    //================================================

    private static Character getCharacter(String code) {
        for (Map.Entry < Character, String > entry: hafMap.entrySet()) {
            if (entry.getValue().equals(code)) {
                return entry.getKey();
            }
        }
        return null;
    }
}

//================================================

class BitOutputStream {
    private OutputStream output;
    private int currentByte;
    private int numBitsFilled;

    public BitOutputStream(OutputStream out) {
        output = out;
        currentByte = 0;
        numBitsFilled = 0;
    }

    public void writeBit(int bit) throws IOException {
        currentByte = currentByte << 1 | bit;
        numBitsFilled++;
        if (numBitsFilled == 8) {
            output.write(currentByte);
            currentByte = 0;
            numBitsFilled = 0;
        }
    }

    public void close() throws IOException {
        while (numBitsFilled != 0)
            writeBit(0);
        output.close();
    }
}

class BitInputStream {
    private InputStream input;
    private int currentByte;
    private int numBitsRemaining;

    public BitInputStream(InputStream in ) {

        input = in;
        currentByte = 0;
        numBitsRemaining = 0;
    }

    public int readBit() throws IOException {
        if (currentByte == -1)
            return -1;
        if (numBitsRemaining == 0) {
            currentByte = input.read();
            if (currentByte == -1)
                return -1;
            numBitsRemaining = 8;
        }
        numBitsRemaining--;
        return (currentByte >>> numBitsRemaining) & 1;
    }

    public void close() throws IOException {
        input.close();
    }
}