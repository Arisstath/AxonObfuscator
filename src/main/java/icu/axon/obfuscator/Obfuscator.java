package icu.axon.obfuscator;


import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import icu.axon.obfuscator.impl.CompilerOptions;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.Prototype;
import org.luaj.vm2.compiler.AxonDumpState;
import org.luaj.vm2.compiler.AxonLuaCompiler;
import org.luaj.vm2.compiler.DumpState;
import org.luaj.vm2.compiler.LuaC;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import static spark.Spark.*;

public class Obfuscator {



    private static String processScript(InputStream script, OutputStream out, CompilerOptions opts) throws Exception {
        //try {
            // create the chunk
            Prototype chunk = AxonLuaCompiler.compile(script, "=axon", opts);
           // Print.printCode(chunk);
            AxonDumpState.dump(chunk, out, true, DumpState.NUMBER_FORMAT_FLOATS_OR_DOUBLES, true, opts);

       /* } catch ( Exception e ) {
            if(e instanceof LuaError) {
               // System.out.println("error: " + e.getMessage());
                return(e.getMessage());
            }
            e.printStackTrace( System.err );
        } finally {
            script.close();
        }*/

       script.close();
        return "";
    }

    private static String minify(String target) throws UnirestException {
        return Unirest.post("http://173.249.40.69:3000/minify").body(target).asString().getBody();
    }


    private static String obfuscate(String input, boolean minify) throws Exception {

        InputStream in = org.apache.commons.io.IOUtils.toInputStream(input, "UTF-8");

        System.out.println("[*] Compiling...");
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        String out = "";
        CompilerOptions opts = new CompilerOptions();
        try {

            out = processScript(in, outputStream, opts);
        } finally {
            outputStream.close();
        }
        //System.out.println(new String(outputStream.toByteArray()));
        StringBuilder buffer = new StringBuilder();

        Random random = new Random();


        char[] chars = "*&^%#@!)(".toCharArray();
        StringBuilder sb = new StringBuilder(20);
        for (int i = 0; i < 2; i++) {
            char c = chars[random.nextInt(chars.length)];
            sb.append(c);
        }

        String sep1 = "&^";
        String sep2 = "(*";
        String sep3 = "@#";

        int concetrations = 0;

        int obf = 0;
        for(byte c : outputStream.toByteArray()) {
            obf++;
            int b = c & 0xff;
            if(obf == 5) {
                if (random.nextBoolean()) {
                    if (random.nextBoolean()) {
                        //Xor the byte by a random number
                        int xor = random.nextInt((50 + 1) - 3) + 3;
                        b = b ^ xor;

                        // buffer.append("|").append(b).append(sep1 + xor);
                        if (concetrations < 15) {
                            buffer.append("\".." + opts.generateCustomString("|") + "..\"").append(b).append(sep1 + xor);
                        } else {
                            // buffer.append("|").. XOR(".append(b).append(sep1 + xor);
                            buffer.append("|").append(b).append(sep1 + xor);
                        }
                        concetrations++;

                    } else {
                        //Increase the byte by a random number
                        int randomNumber = random.nextInt((95 + 1) - 12) + 12;
                        b = b + randomNumber;
                        buffer.append("|").append(b).append(sep2 + randomNumber);
                    }
                } else {
                    //Insert some junk that wont be calculated
                    int randomNumber = random.nextInt((122 + 1) - 3) + 3;
                    buffer.append("|").append(b).append(sep3 + randomNumber);
                }
                obf = 0;
            } else {
                buffer.append("|").append(b);
            }
        }
        buffer.deleteCharAt(0); //remove the first useless character
        String template = new String(Files.readAllBytes(Paths.get("template.axon")));
        template = template.replaceAll("%%SEPARATOR_1%%", sep1);
        template = template.replaceAll("%%SEPARATOR_2%%", sep2);
        template = template.replaceAll("%%SEPARATOR_3%%", sep3);
        template = opts.patchTemplate(template);
        template = template.replaceAll("%%BYTECODE%%", buffer.toString());
        template = template.replaceAll("%%CHUNKRANDOM%%", opts.dumpCustomStack());
        template = template.replaceAll("%%DATASTACK%%", opts.dumpDataStack());
        //template = template.replaceAll("%%CUSTOMORDER%%", opts.dumpCustomDataStack());
        if (minify)
            template = minify(template);
        System.out.println("Compiled");
        return template;
    }

    public static void main(String[] args) throws Exception {

        String s= new String(Files.readAllBytes(Paths.get("axon.in")));
        PrintWriter writer = new PrintWriter("axon.out");



        String source = obfuscate(s, false);


        writer.println(source);
        writer.close();


        ipAddress("127.0.0.1");

        port(8491);
        exception(Exception.class, (e, request, response) -> {
            final StringWriter sw = new StringWriter();
            final PrintWriter pw = new PrintWriter(sw, true);
            e.printStackTrace(pw);
            System.err.println(sw.getBuffer().toString());
        });
        AtomicBoolean obfuscating = new AtomicBoolean(false);
        post("/obfuscate", (req, res) -> {
            String scriptObf = "";
            //if (obfuscating.get()) {
              //  res.status(400);
                //return "There's already a script being obfuscated, please wait.";
            //}

            obfuscating.set(true);
            try {
                scriptObf = obfuscate(req.body(), true);

                res.status(200);
                obfuscating.set(false);
                return scriptObf;
            } catch(Exception e) {
                obfuscating.set(false);
                if(e instanceof LuaError) {
                    scriptObf = e.getMessage().toString();
                    res.status(400);
                    return scriptObf;
                }
                return e.getMessage();
            }
        });
    }
}
