package icu.axon.obfuscator.impl;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.luaj.vm2.AxonLua;
import org.luaj.vm2.Lua;
import org.luaj.vm2.LuaString;
import org.luaj.vm2.Prototype;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;

public class CompilerOptions
{

    private LinkedHashMap<String, Integer> opcodes = new LinkedHashMap();
    private LinkedHashMap<String, Integer> opcodesOriginal = new LinkedHashMap();
    private LinkedHashMap<String, Integer> luaValues = new LinkedHashMap();
    private LinkedList<String> chunkOrder = new LinkedList<>();
    private LinkedList<String> stringsCache = new LinkedList<>();
    final int uniqueId = new Random().nextInt(200);
    private LinkedList<String> chunkDataOrder = new LinkedList<>();
    String customStringStuff = "local abcdefg = {";
    private ArrayList<String> opCodeNames = new ArrayList<>();
    public ArrayList<String> customStrings = new ArrayList<>();
    public int currentSize = 0;
    public int customvalue = 0;
    private String enumKeyword = "";
    private ArrayList<Integer> usedOpcodes = new ArrayList<>();
    private LinkedList<Integer> luaP_opmodes = new LinkedList<>();
    public CompilerOptions() {

        char[] enumpool = {'i','I','1','l'};
        enumKeyword = RandomStringUtils.random(20, enumpool);
        while(enumKeyword.startsWith("1")) {
            enumKeyword = RandomStringUtils.random(20, enumpool);
        }
        chunkOrder.add("NAME");
        chunkOrder.add("FIRSTL");
        chunkOrder.add("LASTL");
        chunkOrder.add("UPVALS");
        chunkOrder.add("ARGS");
        chunkOrder.add("VARGS");
        chunkOrder.add("STACK");

        Collections.shuffle(chunkOrder);

        chunkDataOrder.add("CODE");
        chunkDataOrder.add("CONSTANTS");
        chunkDataOrder.add("DEBUG");

        //Collections.shuffle(chunkDataOrder);

        opcodes.put("OP_MOVE", 0);
        opcodes.put("OP_LOADK", 1);
        opcodes.put("OP_LOADBOOL", 2);
        opcodes.put("OP_LOADNIL", 3);
        opcodes.put("OP_GETUPVAL", 4);
        opcodes.put("OP_GETGLOBAL", 5);
        opcodes.put("OP_GETTABLE", 6);
        opcodes.put("OP_SETGLOBAL", 7);
        opcodes.put("OP_SETUPVAL", 8);
        opcodes.put("OP_SETTABLE", 9);
        opcodes.put("OP_NEWTABLE", 10);
        opcodes.put("OP_SELF", 11);
        opcodes.put("OP_ADD", 12);
        opcodes.put("OP_SUB", 13);
        opcodes.put("OP_MUL", 14);
        opcodes.put("OP_DIV", 15);
        opcodes.put("OP_MOD", 16);
        opcodes.put("OP_POW", 17);
        opcodes.put("OP_UNM", 18);
        opcodes.put("OP_NOT", 19);
        opcodes.put("OP_LEN", 20);
        opcodes.put("OP_CONCAT", 21);
        opcodes.put("OP_JMP", 22);
        opcodes.put("OP_EQ", 23);
        opcodes.put("OP_LT", 24);
        opcodes.put("OP_LE", 25);
        opcodes.put("OP_TEST", 26);
        opcodes.put("OP_TESTSET", 27);
        opcodes.put("OP_CALL", 28);
        opcodes.put("OP_TAILCALL", 29);
        opcodes.put("OP_RETURN", 30);
        opcodes.put("OP_FORLOOP", 31);
        opcodes.put("OP_FORPREP", 32);
        opcodes.put("OP_TFORLOOP", 33);
        opcodes.put("OP_SETLIST", 34);
        opcodes.put("OP_CLOSE", 35);
        opcodes.put("OP_CLOSURE", 36);
        opcodes.put("OP_VARARG", 37);

        opcodesOriginal = (LinkedHashMap<String, Integer>) opcodes.clone();
        luaValues.put("TNIL", 0);
        luaValues.put("TBOOLEAN", 1);
        luaValues.put("TNUMBER", 3);
        luaValues.put("TSTRING", 4);
        luaValues.put("TTABLE", 5);
        luaValues.put("TFUNCTION", 6);
        luaValues.put("TUSERDATA", 7);
        luaValues.put("TTHREAD", 9);
        //System.out.println("Now shuffling, pray for it to work!");
        shuffleMap(opcodes);
        //shuffleMap(luaValues);
        int i = -1;
        for(int opmode : Lua.luaP_opmodes) {
            luaP_opmodes.add(opmode);
        }
        for(String opcode : opcodes.keySet()) {
            i++;
            luaP_opmodes.set(opcodes.get(opcode), Lua.luaP_opmodes[i]);
            //System.out.println("(" + opcode + ") [" + i + "]: " + opcodes.get(opcode));
        }


        //opcode names lol
        ArrayList<String> opcodenames = new ArrayList<>(Arrays.asList("are u foken blind u cant see me?!", "axon > all", "wow ur bad", "i <3 u", "u <3 me", "lalala"));

        for (int a=0;a<3;a++) {
            int val = new Random().nextInt(opcodenames.size());
            String code = opcodenames.get(val);
            opcodenames.remove(val);

            System.out.println("Opcode #"+a + " = " + code);
            opCodeNames.add(generateCustomString(code));
        }

        Collections.shuffle(opCodeNames);

    }

    int lololo = 0;
    ArrayList<Integer> values = new ArrayList<>();
    public String generateCustomString(String current) {
        String s = "jsddshsuidsjkds({";
        for (byte b : current.getBytes()) {
            int value = new Random().nextInt(1234);

            values.add(lololo, value);

            //customStringStuff += "["+lololo+"] = "+value+";";
            s += "dddddddd("+(b^value)+","+values.size()+");";
            lololo++;

        }
        s += "})";

        return s;
    }


    private String generateLuaOpcodes() {

        System.out.println("[!!] Used opcodes: " + usedOpcodes.size() + " " + usedOpcodes.toString());
        String lua = "local Opcode = {";

        for(String opcode : opcodes.keySet()) {
            if(!usedOpcodes.contains(opcodesOriginal.get(opcode))) {
               // continue;
            }
            if(AxonLua.getOpMode(opcodes.get(opcode), this) == AxonLua.iABC) {
                lua += "[" + opcodes.get(opcode) + "] = " + opCodeNames.get(0) + ",";
            } else if (AxonLua.getOpMode(opcodes.get(opcode), this) == AxonLua.iABx) {
                lua += "[" + opcodes.get(opcode) + "] = " + opCodeNames.get(1) + ",";
            } else {
                lua += "[" + opcodes.get(opcode) + "] = " + opCodeNames.get(2) + ",";
            }
        }
        lua+= "}";
        return lua;
    }

    public String dumpDataStack() throws IOException {
        String chunksLua="";
        for(String s : getChunkDataOrder()) {
            System.out.println(s);
            if (s.equals("CODE")) {
                chunksLua += "for Idx = 1, gInt() do -- Loading instructions to the chunk.\n" +
                        "\t\t\tlocal Data\t= gBits32();\n" +
                        "\t\t\tlocal Opco\t= gBit(Data, 1, 6);\n" +
                        "\t\t\tlocal Type\t= Opcode[Opco];\n" +
                        "\t\t\tlocal Inst\t= {\n" +
                        "\t\t\t\tValue\t= Data;\n" +
                        "\t\t\t\t" + enumKeyword + "t= Opco;\n" +
                        "\t\t\t\tgBit(Data, 7, 14); -- Register A.\n" +
                        "\t\t\t};\n" +
                        "\n" +
                        "\t\t\tif (Type == jsddshsuidsjkds({dddddddd(225,7);dddddddd(97,8);dddddddd(820,9);dddddddd(849,10);dddddddd(238,11);dddddddd(908,12);dddddddd(796,13);dddddddd(284,14);dddddddd(149,15);dddddddd(474,16);})) then -- Most common, basic instruction type.\n" +
                        "\t\t\t\tInst[2]\t= gBit(Data, 24, 32);\n" +
                        "\t\t\t\tInst[3]\t= gBit(Data, 15, 23);\n" +
                        "\t\t\telseif (Type == jsddshsuidsjkds({dddddddd(955,1);dddddddd(1174,2);dddddddd(1027,3);dddddddd(518,4);dddddddd(712,5);dddddddd(844,6);})) then\n" +
                        "\t\t\t\tInst[2]\t= gBit(Data, 15, 32);\n" +
                        "\t\t\telseif (Type == jsddshsuidsjkds({dddddddd(155,17);dddddddd(59,18);dddddddd(1,19);dddddddd(226,20);dddddddd(303,21);dddddddd(876,22);dddddddd(444,23);dddddddd(1108,24);dddddddd(1222,25);dddddddd(651,26);})) then\n" +
                        "\t\t\t\tInst[2]\t= gBit(Data, 15, 32) - 131071;\n" +
                        "\t\t\tend;\n" +
                        "\n" +
                        "\t\t\tInstr[Idx]\t= Inst;\n" +
                        "\t\tend;\n";
            }
            if (s.equals("CONSTANTS")) {
                chunksLua += "for Idx = 1, gInt() do -- Load constants.\n" +
                        "\t\t\tlocal Type\t= gBits8();\n" +
                        "\t\t\tlocal Cons;\n" +
                        "\n" +
                        "\t\t\tif (Type == 1) then -- Boolean\n" +
                        "\t\t\t\tCons\t= (gBits8() ~= 0);\n" +
                        "\t\t\telseif (Type == 3) then -- Float/Double\n" +
                        "\t\t\t\tCons\t= gFloat();\n" +
                        "\t\t\telseif (Type == 4) then\n" +
                        "\t\t\t\tCons\t= Sub(gString(), 1, -2);\n" +
                        "\t\t\tend;\n" +
                        "\n" +
                        "\t\t\tConst[Idx - 1]\t= Cons;\n" +
                        "\t\tend;\n" +
                        "\n" +
                        "\t\tfor Idx = 1, gInt() do -- Nested function prototypes.\n" +
                        "\t\t\tProto[Idx - 1]\t= ChunkDecode();\n" +
                        "\t\tend;\n";
            }
            if (s.equals("DEBUG")) {
                chunksLua += "do -- Debugging\n" +
                        "\t\t\tlocal Lines\t= Chunk.Lines;\n" +
                        "\n" +
                        "\t\t\tfor Idx = 1, gInt() do\n" +
                        "\t\t\t\tLines[Idx]\t= gBits32();\n" +
                        "\t\t\tend;\n" +
                        "\n" +
                        "\t\t\tfor _ = 1, gInt() do -- Locals in stack.\n" +
                        "\t\t\t\tgString(); -- Name of local.\n" +
                        "\t\t\t\tgBits32(); -- Starting point.\n" +
                        "\t\t\t\tgBits32(); -- End point.\n" +
                        "\t\t\tend;\n" +
                        "\n" +
                        "\t\t\tfor _ = 1, gInt() do -- Upvalues.\n" +
                        "\t\t\t\tgString(); -- Name of upvalue.\n" +
                        "\t\t\tend;\n" +
                        "\t\tend;\n";
            }

        }

        return chunksLua;
    }

    public String dumpCustomStack() throws IOException {
        String chunksLua="";
        for(String s : getChunkOrder()) {
            //System.out.println("Dumping: " + s);
            if (s.equals("NAME")) {
                chunksLua += "Name = gString();\n";
            }
            if (s.equals("FIRSTL")) {
                chunksLua += "FirstL = gInt();\n";
            }
            if (s.equals("LASTL")) {
                chunksLua += "LastL = gInt();\n";
            }
            if (s.equals("UPVALS")) {
                chunksLua += "Upvals = gBits8();\n";
            }
            if (s.equals("ARGS")) {
                chunksLua += "Args = gBits8();\n";
            }
            if (s.equals("VARGS")) {
                chunksLua += "Vargs = gBits8();\n";
            }
            if (s.equals("STACK")) {
                chunksLua += "Stack    = gBits8();\n";
            }
        }

        return chunksLua;
    }

    public String patchTemplate(String template) {
        template = template.replaceAll("%%ENUMVARIABLE%%", enumKeyword);
        String vmParser = "";
        HashMap<Integer, String> vmInstr = new HashMap<>();
        vmInstr.put(0, "if (" + enumKeyword + " == %%OP_MOVE%%) then -- MOVE\n\n" +
                "                        \t\t\t\t\tStack[Inst[1]]\t= Stack[Inst[2]]; end");
        vmInstr.put(1, "if (" + enumKeyword + " == %%OP_LOADK%%) then -- LOADK\n" +
                "                        \n" +
                "                        \t\t\t\t\tStack[Inst[1]]\t= Const[Inst[2]]; end");
        vmInstr.put(2, "if (" + enumKeyword + " == %%OP_LOADBOOL%%) then -- LOADBOOL\n\n" +
                "                        \t\t\t\t\tStack[Inst[1]]\t= (Inst[2] ~= 0);\n " +
                "                        \n" +
                "                        \t\t\t\t\tif (Inst[3] ~= 0) then \n" +
                "                        \t\t\t\t\t\tInstrPoint\t= InstrPoint + 1; " +
                "                        \t\t\t\t\tend; end");
        vmInstr.put(3, "if (" + enumKeyword + " == %%OP_LOADNIL%%) then local Stk\t= Stack;\n" +
                "\n" +
                "\t\t\t\t\tfor Idx = Inst[1], Inst[2] do\n" +
                "\t\t\t\t\t\tStk[Idx]\t= nil;\n" +
                "\t\t\t\t\tend; end;");
        vmInstr.put(4, "if (" + enumKeyword + "== %%OP_GETUPVAL%%) then " +
                "                        \t\t\t\t\tStack[Inst[1]]\t= Upvalues[Inst[2]]; end");
        vmInstr.put(5, "if (" + enumKeyword + " == %%OP_GETGLOBAL%%) then -- GETGLOBAL\n" +
                "                        \t\t\t\t\tStack[Inst[1]]\t= Env[Const[Inst[2]]]; end");
        vmInstr.put(6, "if (" + enumKeyword + " == %%OP_GETTABLE%%) then -- GETTABLE\n" +
                "\t\t\t\t\tlocal C\t\t= Inst[3];\n" +
                "\t\t\t\t\tlocal Stk\t= Stack;\n" +
                "\n" +
                "\t\t\t\t\tif (C > 255) then\n" +
                "\t\t\t\t\t\tC\t= Const[C - 256];\n" +
                "\t\t\t\t\telse\n" +
                "\t\t\t\t\t\tC\t= Stk[C];\n" +
                "\t\t\t\t\tend;\n" +
                "\n" +
                "\t\t\t\t\tStk[Inst[1]]\t= Stk[Inst[2]][C]; end");
        vmInstr.put(7, "if (" + enumKeyword + " == %%OP_SETGLOBAL%%) then -- SETGLOBAL\n" +
                "\t\t\t\t\tEnv[Const[Inst[2]]]\t= Stack[Inst[1]]; end");
        vmInstr.put(8, "if (" + enumKeyword + " == %%OP_SETUPVAL%%) then -- SETUPVAL\n" +
                "\t\t\t\t\tUpvalues[Inst[2]]\t= Stack[Inst[1]]; end");
        vmInstr.put(9, "if (" + enumKeyword + " == %%OP_SETTABLE%%) then -- SETTABLE\n" +
                "\t\t\t\t\tlocal B, C\t= Inst[2], Inst[3];\n" +
                "\t\t\t\t\tlocal Stk\t= Stack;\n" +
                "\n" +
                "\t\t\t\t\tif (B > 255) then\n" +
                "\t\t\t\t\t\tB\t= Const[B - 256];\n" +
                "\t\t\t\t\telse\n" +
                "\t\t\t\t\t\tB\t= Stk[B];\n" +
                "\t\t\t\t\tend;\n" +
                "\n" +
                "\t\t\t\t\tif (C > 255) then\n" +
                "\t\t\t\t\t\tC\t= Const[C - 256];\n" +
                "\t\t\t\t\telse\n" +
                "\t\t\t\t\t\tC\t= Stk[C];\n" +
                "\t\t\t\t\tend;\n" +
                "\n" +
                "\t\t\t\t\tStk[Inst[1]][B]\t= C; end");
        vmInstr.put(10, "if (" + enumKeyword + " == %%OP_NEWTABLE%%) then -- NEWTABLE\n" +
                "\t\t\t\t\tStack[Inst[1]]\t= {}; end");
        vmInstr.put(11, "if (" + enumKeyword + " == %%OP_SELF%%) then -- SELF\n" +
                "\t\t\t\t\tlocal A\t\t= Inst[1];\n" +
                "\t\t\t\t\tlocal B\t\t= Inst[2];\n" +
                "\t\t\t\t\tlocal C\t\t= Inst[3];\n" +
                "\t\t\t\t\tlocal Stk\t= Stack;\n" +
                "\n" +
                "\t\t\t\t\tB = Stk[B];\n" +
                "\n" +
                "\t\t\t\t\tif (C > 255) then\n" +
                "\t\t\t\t\t\tC\t= Const[C - 256];\n" +
                "\t\t\t\t\telse\n" +
                "\t\t\t\t\t\tC\t= Stk[C];\n" +
                "\t\t\t\t\tend;\n" +
                "\n" +
                "\t\t\t\t\tStk[A + 1]\t= B;\n" +
                "\t\t\t\t\tStk[A]\t\t= B[C]; end");
        vmInstr.put(12, "if (" + enumKeyword + " == %%OP_ADD%%) then -- ADD\n" +
                "\t\t\t\t\tlocal B\t= Inst[2];\n" +
                "\t\t\t\t\tlocal C\t= Inst[3];\n" +
                "\t\t\t\t\tlocal Stk = Stack;\n" +
                "\n" +
                "\t\t\t\t\tif (B > 255) then\n" +
                "\t\t\t\t\t\tB\t= Const[B - 256];\n" +
                "\t\t\t\t\telse\n" +
                "\t\t\t\t\t\tB\t= Stk[B];\n" +
                "\t\t\t\t\tend;\n" +
                "\n" +
                "\t\t\t\t\tif (C > 255) then\n" +
                "\t\t\t\t\t\tC\t= Const[C - 256];\n" +
                "\t\t\t\t\telse\n" +
                "\t\t\t\t\t\tC\t= Stk[C];\n" +
                "\t\t\t\t\tend;\n" +
                "\n" +
                "\t\t\t\t\tStk[Inst[1]]\t= B + C; end");
        vmInstr.put(13, "if (" + enumKeyword + " == %%OP_SUB%%) then -- SUB\n" +
                "\t\t\t\t\tlocal B\t= Inst[2];\n" +
                "\t\t\t\t\tlocal C\t= Inst[3];\n" +
                "\t\t\t\t\tlocal Stk = Stack;\n" +
                "\n" +
                "\t\t\t\t\tif (B > 255) then\n" +
                "\t\t\t\t\t\tB\t= Const[B - 256];\n" +
                "\t\t\t\t\telse\n" +
                "\t\t\t\t\t\tB\t= Stk[B];\n" +
                "\t\t\t\t\tend;\n" +
                "\n" +
                "\t\t\t\t\tif (C > 255) then\n" +
                "\t\t\t\t\t\tC\t= Const[C - 256];\n" +
                "\t\t\t\t\telse\n" +
                "\t\t\t\t\t\tC\t= Stk[C];\n" +
                "\t\t\t\t\tend;\n" +
                "\n" +
                "\t\t\t\t\tStk[Inst[1]]\t= B - C; end");
        vmInstr.put(14, "if (" + enumKeyword + " == %%OP_MUL%%) then -- MUL\n" +
                "\t\t\t\t\tlocal B\t= Inst[2];\n" +
                "\t\t\t\t\tlocal C\t= Inst[3];\n" +
                "\t\t\t\t\tlocal Stk = Stack;\n" +
                "\n" +
                "\t\t\t\t\tif (B > 255) then\n" +
                "\t\t\t\t\t\tB\t= Const[B - 256];\n" +
                "\t\t\t\t\telse\n" +
                "\t\t\t\t\t\tB\t= Stk[B];\n" +
                "\t\t\t\t\tend;\n" +
                "\n" +
                "\t\t\t\t\tif (C > 255) then\n" +
                "\t\t\t\t\t\tC\t= Const[C - 256];\n" +
                "\t\t\t\t\telse\n" +
                "\t\t\t\t\t\tC\t= Stk[C];\n" +
                "\t\t\t\t\tend;\n" +
                "\n" +
                "\t\t\t\t\tStk[Inst[1]]\t= B * C; end");
        vmInstr.put(15, "if (" + enumKeyword + " == %%OP_DIV%%) then -- DIV\n" +
                "\t\t\t\t\tlocal B\t= Inst[2];\n" +
                "\t\t\t\t\tlocal C\t= Inst[3];\n" +
                "\t\t\t\t\tlocal Stk = Stack;\n" +
                "\n" +
                "\t\t\t\t\tif (B > 255) then\n" +
                "\t\t\t\t\t\tB\t= Const[B - 256];\n" +
                "\t\t\t\t\telse\n" +
                "\t\t\t\t\t\tB\t= Stk[B];\n" +
                "\t\t\t\t\tend;\n" +
                "\n" +
                "\t\t\t\t\tif (C > 255) then\n" +
                "\t\t\t\t\t\tC\t= Const[C - 256];\n" +
                "\t\t\t\t\telse\n" +
                "\t\t\t\t\t\tC\t= Stk[C];\n" +
                "\t\t\t\t\tend;\n" +
                "\n" +
                "\t\t\t\t\tStk[Inst[1]]\t= B / C; end");
        vmInstr.put(16, "if (" + enumKeyword + " == %%OP_MOD%%) then -- MOD\n" +
                "\t\t\t\t\tlocal B\t= Inst[2];\n" +
                "\t\t\t\t\tlocal C\t= Inst[3];\n" +
                "\t\t\t\t\tlocal Stk = Stack;\n" +
                "\n" +
                "\t\t\t\t\tif (B > 255) then\n" +
                "\t\t\t\t\t\tB\t= Const[B - 256];\n" +
                "\t\t\t\t\telse\n" +
                "\t\t\t\t\t\tB\t= Stk[B];\n" +
                "\t\t\t\t\tend;\n" +
                "\n" +
                "\t\t\t\t\tif (C > 255) then\n" +
                "\t\t\t\t\t\tC\t= Const[C - 256];\n" +
                "\t\t\t\t\telse\n" +
                "\t\t\t\t\t\tC\t= Stk[C];\n" +
                "\t\t\t\t\tend;\n" +
                "\n" +
                "\t\t\t\t\tStk[Inst[1]]\t= B % C; end");
        vmInstr.put(17, "if (" + enumKeyword + " == %%OP_POW%%) then -- POW\n" +
                "\t\t\t\t\tlocal B\t= Inst[2];\n" +
                "\t\t\t\t\tlocal C\t= Inst[3];\n" +
                "\t\t\t\t\tlocal Stk = Stack;\n" +
                "\n" +
                "\t\t\t\t\tif (B > 255) then\n" +
                "\t\t\t\t\t\tB\t= Const[B - 256];\n" +
                "\t\t\t\t\telse\n" +
                "\t\t\t\t\t\tB\t= Stk[B];\n" +
                "\t\t\t\t\tend;\n" +
                "\n" +
                "\t\t\t\t\tif (C > 255) then\n" +
                "\t\t\t\t\t\tC\t= Const[C - 256];\n" +
                "\t\t\t\t\telse\n" +
                "\t\t\t\t\t\tC\t= Stk[C];\n" +
                "\t\t\t\t\tend;\n" +
                "\n" +
                "\t\t\t\t\tStk[Inst[1]]\t= B ^ C; end");
        vmInstr.put(18, "if (" + enumKeyword + " == %%OP_UNM%%) then -- UNM\n" +
                "\t\t\t\t\tStack[Inst[1]]\t= -Stack[Inst[2]]; end");
        vmInstr.put(19, "if (" + enumKeyword + " == %%OP_NOT%%) then -- NOT\n" +
                "\t\t\t\t\tStack[Inst[1]]\t= (not Stack[Inst[2]]); end");
        vmInstr.put(20, "if (" + enumKeyword + " == %%OP_LEN%%) then -- LEN\n" +
                "\t\t\t\t\tStack[Inst[1]]\t= #Stack[Inst[2]]; end");
        vmInstr.put(21, "if (" + enumKeyword + " == %%OP_CONCAT%%) then -- CONCAT\n" +
                "\t\t\t\t\tlocal Stk\t= Stack;\n" +
                "\t\t\t\t\tlocal B\t\t= Inst[2];\n" +
                "\t\t\t\t\tlocal K \t= Stk[B];\n" +
                "\n" +
                "\t\t\t\t\tfor Idx = B + 1, Inst[3] do\n" +
                "\t\t\t\t\t\tK = K .. Stk[Idx];\n" +
                "\t\t\t\t\tend;\n" +
                "\n" +
                "\t\t\t\t\tStack[Inst[1]]\t= K; end");
        vmInstr.put(22, "if (" + enumKeyword + " == %%OP_JMP%%) then -- JMP\n" +
                "\t\t\t\t\tInstrPoint\t= InstrPoint + Inst[2]; end");
        vmInstr.put(23, "if (" + enumKeyword + " == %%OP_EQ%%) then -- EQ\n" +
                "\t\t\t\t\tlocal A\t= Inst[1] ~= 0;\n" +
                "\t\t\t\t\tlocal B\t= Inst[2];\n" +
                "\t\t\t\t\tlocal C\t= Inst[3];\n" +
                "\t\t\t\t\tlocal Stk = Stack;\n" +
                "\n" +
                "\t\t\t\t\tif (B > 255) then\n" +
                "\t\t\t\t\t\tB\t= Const[B - 256];\n" +
                "\t\t\t\t\telse\n" +
                "\t\t\t\t\t\tB\t= Stk[B];\n" +
                "\t\t\t\t\tend;\n" +
                "\n" +
                "\t\t\t\t\tif (C > 255) then\n" +
                "\t\t\t\t\t\tC\t= Const[C - 256];\n" +
                "\t\t\t\t\telse\n" +
                "\t\t\t\t\t\tC\t= Stk[C];\n" +
                "\t\t\t\t\tend;\n" +
                "\n" +
                "\t\t\t\t\tif (B == C) ~= A then\n" +
                "\t\t\t\t\t\tInstrPoint\t= InstrPoint + 1;\n" +
                "\t\t\t\t\tend; end");
        vmInstr.put(24, "if (" + enumKeyword + " == %%OP_LT%%) then -- LT\n" +
                "\t\t\t\t\tlocal A\t= Inst[1] ~= 0;\n" +
                "\t\t\t\t\tlocal B\t= Inst[2];\n" +
                "\t\t\t\t\tlocal C\t= Inst[3];\n" +
                "\t\t\t\t\tlocal Stk = Stack;\n" +
                "\n" +
                "\t\t\t\t\tif (B > 255) then\n" +
                "\t\t\t\t\t\tB\t= Const[B - 256];\n" +
                "\t\t\t\t\telse\n" +
                "\t\t\t\t\t\tB\t= Stk[B];\n" +
                "\t\t\t\t\tend;\n" +
                "\n" +
                "\t\t\t\t\tif (C > 255) then\n" +
                "\t\t\t\t\t\tC\t= Const[C - 256];\n" +
                "\t\t\t\t\telse\n" +
                "\t\t\t\t\t\tC\t= Stk[C];\n" +
                "\t\t\t\t\tend;\n" +
                "\n" +
                "\t\t\t\t\tif (B < C) ~= A then\n" +
                "\t\t\t\t\t\tInstrPoint\t= InstrPoint + 1;\n" +
                "\t\t\t\t\tend; end");
        vmInstr.put(25, "if (" + enumKeyword + " == %%OP_LE%%) then -- LE\n" +
                "\t\t\t\t\tlocal A\t= Inst[1] ~= 0;\n" +
                "\t\t\t\t\tlocal B\t= Inst[2];\n" +
                "\t\t\t\t\tlocal C\t= Inst[3];\n" +
                "\t\t\t\t\tlocal Stk = Stack;\n" +
                "\n" +
                "\t\t\t\t\tif (B > 255) then\n" +
                "\t\t\t\t\t\tB\t= Const[B - 256];\n" +
                "\t\t\t\t\telse\n" +
                "\t\t\t\t\t\tB\t= Stk[B];\n" +
                "\t\t\t\t\tend;\n" +
                "\n" +
                "\t\t\t\t\tif (C > 255) then\n" +
                "\t\t\t\t\t\tC\t= Const[C - 256];\n" +
                "\t\t\t\t\telse\n" +
                "\t\t\t\t\t\tC\t= Stk[C];\n" +
                "\t\t\t\t\tend;\n" +
                "\n" +
                "\t\t\t\t\tif (B <= C) ~= A then\n" +
                "\t\t\t\t\t\tInstrPoint\t= InstrPoint + 1;\n" +
                "\t\t\t\t\tend; end");
        vmInstr.put(26, "if (" + enumKeyword + " == %%OP_TEST%%) then -- TEST\n" +
                "\t\t\t\t\tif (not not Stack[Inst[1]]) == (Inst[3] == 0) then\n" +
                "\t\t\t\t\t\tInstrPoint\t= InstrPoint + 1;\n" +
                "\t\t\t\t\tend;\n" +
                "\t\t\t\tend");
        vmInstr.put(27, "if (" + enumKeyword + " == %%OP_TESTSET%%) then local B\t= Stack[Inst[2]];\n" +
                "\n" +
                "\t\t\t\t\tif (not not B) == (Inst[3] == 0) then\n" +
                "\t\t\t\t\t\tInstrPoint\t= InstrPoint + 1;\n" +
                "\t\t\t\t\telse\n" +
                "\t\t\t\t\t\tStack[Inst[1]] = B;\n" +
                "\t\t\t\t\tend; end");
        vmInstr.put(28, "if (" + enumKeyword + " == %%OP_CALL%%) then -- CALL\n" +
                "\t\t\t\t\tlocal A\t= Inst[1];\n" +
                "\t\t\t\t\tlocal B\t= Inst[2];\n" +
                "\t\t\t\t\tlocal C\t= Inst[3];\n" +
                "\t\t\t\t\tlocal Stk\t= Stack;\n" +
                "\t\t\t\t\tlocal Args, Results;\n" +
                "\t\t\t\t\tlocal Limit, Edx;\n" +
                "\n" +
                "\t\t\t\t\tArgs\t= {};\n" +
                "\n" +
                "\t\t\t\t\tif (B ~= 1) then\n" +
                "\t\t\t\t\t\tif (B ~= 0) then\n" +
                "\t\t\t\t\t\t\tLimit = A + B - 1;\n" +
                "\t\t\t\t\t\telse\n" +
                "\t\t\t\t\t\t\tLimit = Top;\n" +
                "\t\t\t\t\t\tend;\n" +
                "\n" +
                "\t\t\t\t\t\tEdx\t= 0;\n" +
                "\n" +
                "\t\t\t\t\t\tfor Idx = A + 1, Limit do\n" +
                "\t\t\t\t\t\t\tEdx = Edx + 1;\n" +
                "\n" +
                "\t\t\t\t\t\t\tArgs[Edx] = Stk[Idx];\n" +
                "\t\t\t\t\t\tend;\n" +
                "\n" +
                "\t\t\t\t\t\tLimit, Results = _Returns(Stk[A](unpack(Args, 1, Limit - A)));\n" +
                "\t\t\t\t\telse\n" +
                "\t\t\t\t\t\tLimit, Results = _Returns(Stk[A]());\n" +
                "\t\t\t\t\tend;\n" +
                "\n" +
                "\t\t\t\t\tTop = A - 1;\n" +
                "\n" +
                "\t\t\t\t\tif (C ~= 1) then\n" +
                "\t\t\t\t\t\tif (C ~= 0) then\n" +
                "\t\t\t\t\t\t\tLimit = A + C - 2;\n" +
                "\t\t\t\t\t\telse\n" +
                "\t\t\t\t\t\t\tLimit = Limit + A - 1;\n" +
                "\t\t\t\t\t\tend;\n" +
                "\n" +
                "\t\t\t\t\t\tEdx\t= 0;\n" +
                "\n" +
                "\t\t\t\t\t\tfor Idx = A, Limit do\n" +
                "\t\t\t\t\t\t\tEdx = Edx + 1;\n" +
                "\n" +
                "\t\t\t\t\t\t\tStk[Idx] = Results[Edx];\n" +
                "\t\t\t\t\t\tend;\n" +
                "\t\t\t\t\tend; end;");
        vmInstr.put(29, "if (" + enumKeyword + " == %%OP_TAILCALL%%) then -- TAILCALL\n" +
                "\t\t\t\t\tlocal A\t= Inst[1];\n" +
                "\t\t\t\t\tlocal B\t= Inst[2];\n" +
                "\t\t\t\t\tlocal Stk\t= Stack;\n" +
                "\t\t\t\t\tlocal Args, Results;\n" +
                "\t\t\t\t\tlocal Limit;\n" +
                "\t\t\t\t\tlocal Rets = 0;\n" +
                "\n" +
                "\t\t\t\t\tArgs = {};\n" +
                "\n" +
                "\t\t\t\t\tif (B ~= 1) then\n" +
                "\t\t\t\t\t\tif (B ~= 0) then\n" +
                "\t\t\t\t\t\t\tLimit = A + B - 1;\n" +
                "\t\t\t\t\t\telse\n" +
                "\t\t\t\t\t\t\tLimit = Top;\n" +
                "\t\t\t\t\t\tend\n" +
                "\n" +
                "\t\t\t\t\t\tfor Idx = A + 1, Limit do\n" +
                "\t\t\t\t\t\t\tArgs[#Args + 1] = Stk[Idx];\n" +
                "\t\t\t\t\t\tend\n" +
                "\n" +
                "\t\t\t\t\t\tResults = {Stk[A](unpack(Args, 1, Limit - A))};\n" +
                "\t\t\t\t\telse\n" +
                "\t\t\t\t\t\tResults = {Stk[A]()};\n" +
                "\t\t\t\t\tend;\n" +
                "\n" +
                "\t\t\t\t\tfor Index in next, Results do -- get return count\n" +
                "\t\t\t\t\t\tif (Index > Rets) then\n" +
                "\t\t\t\t\t\t\tRets = Index;\n" +
                "\t\t\t\t\t\tend;\n" +
                "\t\t\t\t\tend;\n" +
                "\n" +
                "\t\t\t\t\treturn Results, Rets; end");
        vmInstr.put(30, "if (" + enumKeyword + " == %%OP_RETURN%%) then -- RETURN\n" +
                "\t\t\t\t\tlocal A\t= Inst[1];\n" +
                "\t\t\t\t\tlocal B\t= Inst[2];\n" +
                "\t\t\t\t\tlocal Stk\t= Stack;\n" +
                "\t\t\t\t\tlocal Edx, Output;\n" +
                "\t\t\t\t\tlocal Limit;\n" +
                "\n" +
                "\t\t\t\t\tif (B == 1) then\n" +
                "\t\t\t\t\t\treturn;\n" +
                "\t\t\t\t\telseif (B == 0) then\n" +
                "\t\t\t\t\t\tLimit\t= Top;\n" +
                "\t\t\t\t\telse\n" +
                "\t\t\t\t\t\tLimit\t= A + B - 2;\n" +
                "\t\t\t\t\tend;\n" +
                "\n" +
                "\t\t\t\t\tOutput = {};\n" +
                "\t\t\t\t\tEdx = 0;\n" +
                "\n" +
                "\t\t\t\t\tfor Idx = A, Limit do\n" +
                "\t\t\t\t\t\tEdx\t= Edx + 1;\n" +
                "\n" +
                "\t\t\t\t\t\tOutput[Edx] = Stk[Idx];\n" +
                "\t\t\t\t\tend;\n" +
                "\n" +
                "\t\t\t\t\treturn Output, Edx; end");
        vmInstr.put(31, "if (" + enumKeyword + " == %%OP_FORLOOP%%) then -- FORLOOP\n" +
                "\t\t\t\t\tlocal A\t\t= Inst[1];\n" +
                "\t\t\t\t\tlocal Stk\t= Stack;\n" +
                "\n" +
                "\t\t\t\t\tlocal Step\t= Stk[A + 2];\n" +
                "\t\t\t\t\tlocal Index\t= Stk[A] + Step;\n" +
                "\n" +
                "\t\t\t\t\tStk[A]\t= Index;\n" +
                "\n" +
                "\t\t\t\t\tif (Step > 0) then\n" +
                "\t\t\t\t\t\tif Index <= Stk[A + 1] then\n" +
                "\t\t\t\t\t\t\tInstrPoint\t= InstrPoint + Inst[2];\n" +
                "\n" +
                "\t\t\t\t\t\t\tStk[A + 3] = Index;\n" +
                "\t\t\t\t\t\tend;\n" +
                "\t\t\t\t\telse\n" +
                "\t\t\t\t\t\tif Index >= Stk[A + 1] then\n" +
                "\t\t\t\t\t\t\tInstrPoint\t= InstrPoint + Inst[2];\n" +
                "\n" +
                "\t\t\t\t\t\t\tStk[A + 3] = Index;\n" +
                "\t\t\t\t\t\tend\n" +
                "\t\t\t\t\tend end");
        vmInstr.put(32, "if (" + enumKeyword + " == %%OP_FORPREP%%) then -- FORPREP\n" +
                "\t\t\t\t\tlocal A\t\t= Inst[1];\n" +
                "\t\t\t\t\tlocal Stk\t= Stack;\n" +
                "\n" +
                "\t\t\t\t\t-- As per mirroring the real vm\n" +
                "\t\t\t\t\tStk[A] = assert(tonumber(Stk[A]), '`for` initial value must be a number');\n" +
                "\t\t\t\t\tStk[A + 1] = assert(tonumber(Stk[A + 1]), '`for` limit must be a number');\n" +
                "\t\t\t\t\tStk[A + 2] = assert(tonumber(Stk[A + 2]), '`for` step must be a number');\n" +
                "\n" +
                "\t\t\t\t\tStk[A]\t= Stk[A] - Stk[A + 2];\n" +
                "\n" +
                "\t\t\t\t\tInstrPoint\t= InstrPoint + Inst[2]; end");
        vmInstr.put(33, "if (" + enumKeyword + " == %%OP_TFORLOOP%%) then -- TFORLOOP\n" +
                "\t\t\t\t\tlocal A\t\t= Inst[1];\n" +
                "\t\t\t\t\tlocal C\t\t= Inst[3];\n" +
                "\t\t\t\t\tlocal Stk\t= Stack;\n" +
                "\n" +
                "\t\t\t\t\tlocal Offset\t= A + 2;\n" +
                "\t\t\t\t\tlocal Result\t= {Stk[A](Stk[A + 1], Stk[A + 2])};\n" +
                "\n" +
                "\t\t\t\t\tfor Idx = 1, C do\n" +
                "\t\t\t\t\t\tStack[Offset + Idx] = Result[Idx];\n" +
                "\t\t\t\t\tend;\n" +
                "\n" +
                "\t\t\t\t\tif (Stk[A + 3] ~= nil) then\n" +
                "\t\t\t\t\t\tStk[A + 2]\t= Stk[A + 3];\n" +
                "\t\t\t\t\telse\n" +
                "\t\t\t\t\t\tInstrPoint\t= InstrPoint + 1;\n" +
                "\t\t\t\t\tend; end");
        vmInstr.put(34, "if (" + enumKeyword + " == %%OP_SETLIST%%) then -- SETLIST\n" +
                "\t\t\t\t\tlocal A\t\t= Inst[1];\n" +
                "\t\t\t\t\tlocal B\t\t= Inst[2];\n" +
                "\t\t\t\t\tlocal C\t\t= Inst[3];\n" +
                "\t\t\t\t\tlocal Stk\t= Stack;\n" +
                "\n" +
                "\t\t\t\t\tif (C == 0) then\n" +
                "\t\t\t\t\t\tInstrPoint\t= InstrPoint + 1;\n" +
                "\t\t\t\t\t\tC\t\t\t= Instr[InstrPoint].Value;\n" +
                "\t\t\t\t\tend;\n" +
                "\n" +
                "\t\t\t\t\tlocal Offset\t= (C - 1) * 50;\n" +
                "\t\t\t\t\tlocal T\t\t\t= Stk[A]; -- Assuming T is the newly created table.\n" +
                "\n" +
                "\t\t\t\t\tif (B == 0) then\n" +
                "\t\t\t\t\t\tB\t= Top - A;\n" +
                "\t\t\t\t\tend;\n" +
                "\n" +
                "\t\t\t\t\tfor Idx = 1, B do\n" +
                "\t\t\t\t\t\tT[Offset + Idx] = Stk[A + Idx];\n" +
                "\t\t\t\t\tend; end");
        vmInstr.put(35, "if (" + enumKeyword + " == %%OP_CLOSE%%) then -- CLOSE\n" +
                "\t\t\t\t\tlocal A\t\t= Inst[1];\n" +
                "\t\t\t\t\tlocal Cls\t= {}; -- Slight doubts on any issues this may cause\n" +
                "\n" +
                "\t\t\t\t\tfor Idx = 1, #Lupvals do\n" +
                "\t\t\t\t\t\tlocal List = Lupvals[Idx];\n" +
                "\n" +
                "\t\t\t\t\t\tfor Idz = 0, #List do\n" +
                "\t\t\t\t\t\t\tlocal Upv\t= List[Idz];\n" +
                "\t\t\t\t\t\t\tlocal Stk\t= Upv[1];\n" +
                "\t\t\t\t\t\t\tlocal Pos\t= Upv[2];\n" +
                "\n" +
                "\t\t\t\t\t\t\tif (Stk == Stack) and (Pos >= A) then\n" +
                "\t\t\t\t\t\t\t\tCls[Pos]\t= Stk[Pos];\n" +
                "\t\t\t\t\t\t\t\tUpv[1]\t\t= Cls; -- @memcorrupt credit me for the spoonfeed\n" +
                "\t\t\t\t\t\t\tend;\n" +
                "\t\t\t\t\t\tend;\n" +
                "\t\t\t\t\tend; end");
        vmInstr.put(36, "if (" + enumKeyword + " == %%OP_CLOSURE%%) then -- CLOSURE\n" +
                "\t\t\t\t\tlocal NewProto\t= Proto[Inst[2]];\n" +
                "\t\t\t\t\tlocal Stk\t= Stack;\n" +
                "\n" +
                "\t\t\t\t\tlocal Indexes;\n" +
                "\t\t\t\t\tlocal NewUvals;\n" +
                "\n" +
                "\t\t\t\t\tif (NewProto.Upvals ~= 0) then\n" +
                "\t\t\t\t\t\tIndexes\t\t= {};\n" +
                "\t\t\t\t\t\tNewUvals\t= setmetatable({}, {\n" +
                "\t\t\t\t\t\t\t\t__index = function(_, Key)\n" +
                "\t\t\t\t\t\t\t\t\tlocal Val\t= Indexes[Key];\n" +
                "\n" +
                "\t\t\t\t\t\t\t\t\treturn Val[1][Val[2]];\n" +
                "\t\t\t\t\t\t\t\tend,\n" +
                "\t\t\t\t\t\t\t\t__newindex = function(_, Key, Value)\n" +
                "\t\t\t\t\t\t\t\t\tlocal Val\t= Indexes[Key];\n" +
                "\n" +
                "\t\t\t\t\t\t\t\t\tVal[1][Val[2]]\t= Value;\n" +
                "\t\t\t\t\t\t\t\tend;\n" +
                "\t\t\t\t\t\t\t}\n" +
                "\t\t\t\t\t\t);\n" +
                "\n" +
                "\t\t\t\t\t\tfor Idx = 1, NewProto.Upvals do\n" +
                "\t\t\t\t\t\t\tlocal Mvm\t= Instr[InstrPoint];\n" +
                "\n" +
                "\t\t\t\t\t\t\tif (Mvm." + enumKeyword + " == %%OP_MOVE%%) then -- MOVE\n" +
                "\t\t\t\t\t\t\t\tIndexes[Idx - 1] = {Stk, Mvm[2]};\n" +
                "\t\t\t\t\t\t\telseif (Mvm." + enumKeyword + " == %%OP_GETUPVAL%%) then -- GETUPVAL\n" +
                "\t\t\t\t\t\t\t\tIndexes[Idx - 1] = {Upvalues, Mvm[2]};\n" +
                "\t\t\t\t\t\t\tend;\n" +
                "\n" +
                "\t\t\t\t\t\t\tInstrPoint\t= InstrPoint + 1;\n" +
                "\t\t\t\t\t\tend;\n" +
                "\n" +
                "\t\t\t\t\t\tLupvals[#Lupvals + 1]\t= Indexes;\n" +
                "\t\t\t\t\tend;\n" +
                "\n" +
                "\t\t\t\t\tStk[Inst[1]]\t\t\t= Wrap(NewProto, Env, NewUvals); end");
        vmInstr.put(37, "if (" + enumKeyword + " == %%OP_VARARG%%) then -- VARARG\n" +
                "\t\t\t\t\tlocal A\t= Inst[1];\n" +
                "\t\t\t\t\tlocal B\t= Inst[2];\n" +
                "\t\t\t\t\tlocal Stk, Vars\t= Stack, Vararg;\n" +
                "\n" +
                "\t\t\t\t\tTop = A - 1;\n" +
                "\n" +
                "\t\t\t\t\tfor Idx = A, A + (B > 0 and B - 1 or Varargsz) do\n" +
                "\t\t\t\t\t\tStk[Idx]\t= Vars[Idx - A];\n" +
                "\t\t\t\t\tend;\n" +
                "\t\t\t\tend; end");

        LinkedList<String> usedRn = new LinkedList<>();
        for(int opcode : usedOpcodes) {
            String rn = "";
            rn += vmInstr.get(opcode) + "\n";
            if(new Random().nextBoolean()) {
                if(new Random().nextBoolean()) {
                    rn+=" if aaabbb(aaa(s,3,3)) ~= 114 then  " + enumKeyword + " = " + enumKeyword + "^ 3  end ";
                } else {
                    rn+=" if aaabbb(aaa(s2,#s2/2,#s2/2)) ~= 104 then " + enumKeyword + " = " + enumKeyword + "/ 2 end ";
                }
            }

            usedRn.add(rn);
        }

        Collections.shuffle(usedRn);

        for (String s : usedRn) {
            vmParser += s;
        }


        template = template.replaceAll("%%VMINSTRUCTIONS%%", vmParser);
        for(String opcode : opcodes.keySet()) {
            template = template.replaceAll("%%" + opcode + "%%", "" + generateLuaObfNumber(opcodes.get(opcode))+"");
        }


        String luaChunkRead = "";








        template = template.replaceAll("%%CODES%%", "if (Type == " + opCodeNames.get(0) + ") then -- Most common, basic instruction type.\n" +
                "\t\t\t\tInst[2]\t= gBit(Data, 24, 32);\n" +
                "\t\t\t\tInst[3]\t= gBit(Data, 15, 23);\n" +
                "\t\t\telseif (Type == "+ opCodeNames.get(1) +") then\n" +
                "\t\t\t\tInst[2]\t= gBit(Data, 15, 32);\n" +
                "\t\t\telseif (Type == "+ opCodeNames.get(2) +") then\n" +
                "\t\t\t\tInst[2]\t= gBit(Data, 15, 32) - 131071;\n" +
                "\t\t\tend;");


        template = template.replaceAll("%%UNIQUEID%%", uniqueId+"");
        template = template.replaceAll("%%OPCODES%%", generateLuaOpcodes());


        //custom hardcoded strings
        template = template.replaceAll("%%STRING1%%", generateCustomString("better than luraph lmfao"));
        template = template.replaceAll("%%STRING2%%", generateCustomString("|"));
        template = template.replaceAll("%%STRING3%%", generateCustomString("hello skid, put this whole thing in a base64 decoder"));
        template = template.replaceAll("%%STRING4%%", generateCustomString("#"));
        template = template.replaceAll("%%STRING5%%", generateCustomString("?"));
        template = template.replaceAll("%%STRING6%%", generateCustomString("%s:%s: %s"));
        template = template.replaceAll("%%STRING7%%", generateCustomString("Code"));
        template = template.replaceAll("%%STRING8%%", generateCustomString("Sizet size not supported"));
        template = template.replaceAll("%%STRING9%%", generateCustomString("Integer size not supported"));
        template = template.replaceAll("%%STRING10%%", generateCustomString("AXON bytecode expected."));
        template = template.replaceAll("%%STRING11%%", generateCustomString("\\27AXN"));

        template = template.replaceAll("%%STRING12%%", generateCustomString("\\4\\8\\0"));
        template = template.replaceAll("%%STRING13%%", generateCustomString("Unsupported bytecode target platform"));
        //

System.out.println(values.size());
        for (int i=0;i<values.size();i++)
            customStringStuff += "["+(i+1)+"]="+values.get(i)+";";

        //keep this at bottom
        customStringStuff += "}";
        template = template.replaceAll("%%CUSTOMSTRINGTABLE%%", customStringStuff);

        return template;
    }

    private String generateLuaObfNumber(int number) {
        if(1==1) {
            return  number+"";
        }
        StringBuilder lua = new StringBuilder("#{'axon'");
        Random random = new Random();
        if (number > 50) {
            for(int i = 0; i<50; i++) {
                lua.append(",").append(random.nextInt(10000));
            }
        } else {
            for(int i = 0; i<number-1; i++) {
                lua.append(",").append(random.nextInt(10000));
            }
        }
        // if(1==1) {
        // return number+"";
        //}
        if (number > 50) {
            return lua + "} + " + ((number-1)-50);
        } else {
            return lua + "}";
        }

    }


    private <K,V> void shuffleMap(Map<K,V> map) {
        List<V> valueList = new ArrayList<V>(map.values());
        Collections.shuffle(valueList);
        Iterator<V> valueIt = valueList.iterator();
        for(Map.Entry<K,V> e : map.entrySet()) {
            e.setValue(valueIt.next());
        }
    }

    public int getStringCacheId(String s) {
        if(!stringsCache.contains(s)) {
            stringsCache.addLast(s);
            System.out.println("::DEBUG:: New string added to the cache! '" + s + "'");
            return stringsCache.size() - 1;
        }
        return stringsCache.indexOf(s);
    }

    public LinkedList<String> getChunkOrder() {
        return chunkOrder;
    }
    public LinkedList<String> getChunkDataOrder() {
        return chunkDataOrder;
    }

    public int getLuaValue(String val) {
        return luaValues.get(val);
    }
    public int getOpmode(int originalIndex) {
        return luaP_opmodes.get(originalIndex);
    }
    public int getOpcode(String opcode) {
      //  System.out.println("<- " + opcode + " ->");
        int rOpcode = opcodes.get(opcode); //this is the newly assigned opcode
        int originalOpcode = opcodesOriginal.get(opcode);
       // System.out.println("original -> " + originalOpcode);
        if(!usedOpcodes.contains(originalOpcode)) {
            usedOpcodes.add(originalOpcode);
        }
        return rOpcode;
    }
}
