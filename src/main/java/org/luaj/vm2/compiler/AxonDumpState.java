/*******************************************************************************
* Copyright (c) 2009 Luaj.org. All rights reserved.
*
* Permission is hereby granted, free of charge, to any person obtaining a copy
* of this software and associated documentation files (the "Software"), to deal
* in the Software without restriction, including without limitation the rights
* to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
* copies of the Software, and to permit persons to whom the Software is
* furnished to do so, subject to the following conditions:
*
* The above copyright notice and this permission notice shall be included in
* all copies or substantial portions of the Software.
* 
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
* IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
* FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
* AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
* LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
* OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
* THE SOFTWARE.
******************************************************************************/
package org.luaj.vm2.compiler;

import icu.axon.obfuscator.impl.CompilerOptions;
import icu.axon.obfuscator.util.AxonStringUtil;
import org.luaj.vm2.LocVars;
import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Prototype;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;


public class AxonDumpState {

	/** mark for precompiled code (`<esc>Lua') */
	public static final String LUA_SIGNATURE	= "\033Lua";

	/** for header of binary files -- this is Lua 5.1 */
	public static final int LUAC_VERSION		= 0x51;

	/** for header of binary files -- this is the official format */
	public static final int LUAC_FORMAT		= 0;

	/** size of header of binary files */
	public static final int LUAC_HEADERSIZE		= 12;

	/** expected lua header bytes */
	private static final byte[] LUAC_HEADER_SIGNATURE = { '\033', 'A', 'X', 'N' };

	/** set true to allow integer compilation */
	public static boolean ALLOW_INTEGER_CASTING = false;

	/** format corresponding to non-number-patched lua, all numbers are floats or doubles */
	public static final int NUMBER_FORMAT_FLOATS_OR_DOUBLES    = 0;

	/** format corresponding to non-number-patched lua, all numbers are ints */
	public static final int NUMBER_FORMAT_INTS_ONLY            = 1;

	/** format corresponding to number-patched lua, all numbers are 32-bit (4 byte) ints */
	public static final int NUMBER_FORMAT_NUM_PATCH_INT32      = 4;

	/** default number format */
	public static final int NUMBER_FORMAT_DEFAULT = NUMBER_FORMAT_FLOATS_OR_DOUBLES;

	// header fields
	private boolean IS_LITTLE_ENDIAN = false;
	private int NUMBER_FORMAT = NUMBER_FORMAT_DEFAULT;
	private int SIZEOF_LUA_NUMBER = 8;
	private static final int SIZEOF_INT = 4;
	private static final int SIZEOF_SIZET = 4;
	private static final int SIZEOF_INSTRUCTION = 4;

	DataOutputStream writer;
	boolean strip;
	int status;

	private CompilerOptions opts;
	public AxonDumpState(OutputStream w, boolean strip, CompilerOptions opts) {
		this.writer = new DataOutputStream( w );
		this.strip = strip;
		this.status = 0;
		this.opts = opts;
	}

	void dumpBlock(final byte[] b, int size) throws IOException {
		writer.write(b, 0, size);
	}

	void dumpChar(int b) throws IOException {
		writer.write( b );
	}

	void dumpInt(int x) throws IOException {
		if ( IS_LITTLE_ENDIAN ) {
			writer.writeByte(x&0xff);
			writer.writeByte((x>>8)&0xff);
			writer.writeByte((x>>16)&0xff);
			writer.writeByte((x>>24)&0xff);
		} else {
			writer.writeInt(x);
		}
	}

	void dumpString(LuaString s) throws IOException {
		String str = AxonStringUtil.rot13(s.tojstring());

		int mid = str.length() / 2;
		String[] parts = {str.substring(0, mid),str.substring(mid)};

		LuaString finalString = LuaString.valueOf(parts[0] + "@axn@" + parts[1]);
		int len = finalString.len().toint();
		dumpInt( len+1 );
		finalString.write( writer, 0, len );
		writer.write( 0 );
	}
	void dumpStringSpecial(LuaString s) throws IOException {
		int len = s.len().toint();
		dumpInt( len+1 );
		s.write( writer, 0, len );
		writer.write( 0 );
	}

	void dumpDouble(double d) throws IOException {
		long l = Double.doubleToLongBits(d);
		if ( IS_LITTLE_ENDIAN ) {
			dumpInt( (int) l );
			dumpInt( (int) (l>>32) );
		} else {
			writer.writeLong(l);
		}
	}

	void dumpCode( final Prototype f ) throws IOException {
		final int[] code = f.code;
		int n = code.length;
		dumpInt( n );
		for ( int i=0; i<n; i++ )
			dumpInt( code[i] );
	}

	void dumpConstants(final Prototype f) throws IOException {
		final LuaValue[] k = f.k;
		int i, n = k.length;


		boolean val = new Random().nextBoolean();
		dumpDouble(val ? 1 : 0);
		if(val) {
			//AXON: Fuck up the decompilers seriously
			System.out.println("::DEBUG:: Fucking constants");
			String[] bait = new String[] {"if", "else", "end", "for", "local"};
			for(String xd : bait) {
				LuaValue baitVariable = LuaValue.valueOf(xd);
				writer.write(opts.getLuaValue("TSTRING"));
				dumpStringSpecial((LuaString) baitVariable);
			}
		}
		dumpInt(n+3); //axonvm will get rid of +3
		for (i = 0; i < n; i++) {
			final LuaValue o = k[i];
			int i1 = o.type();
			if (i1 == opts.getLuaValue("TNIL")) {
				writer.write(opts.getLuaValue("TNIL"));

			} else if (i1 == opts.getLuaValue("TBOOLEAN")) {
				writer.write(opts.getLuaValue("TBOOLEAN"));
				dumpChar((!o.toboolean()) ? 1 : 0);

			} else if (i1 == opts.getLuaValue("TNUMBER")) {
				switch (NUMBER_FORMAT) {
					case NUMBER_FORMAT_FLOATS_OR_DOUBLES:
						writer.write(opts.getLuaValue("TNUMBER"));
						dumpDouble(o.todouble());
						break;
					case NUMBER_FORMAT_INTS_ONLY:
						if (!ALLOW_INTEGER_CASTING && !o.isint())
							throw new IllegalArgumentException("not an integer: " + o);
						writer.write(opts.getLuaValue("TNUMBER"));
						dumpInt(o.toint());
						break;
					case NUMBER_FORMAT_NUM_PATCH_INT32:
						if (o.isint()) {
							writer.write(opts.getLuaValue("TINT"));
							dumpInt(o.toint());
						} else {
							writer.write(opts.getLuaValue("TNUMBER"));
							dumpDouble(o.todouble());
						}
						break;
					default:
						throw new IllegalArgumentException("number format not supported: " + NUMBER_FORMAT);
				}

			} else if (i1 == opts.getLuaValue("TSTRING")) {
				writer.write(opts.getLuaValue("TSTRING"));
				dumpString((LuaString) o);

			} else {
				throw new IllegalArgumentException("bad type for " + o);
			}
		}

		n = f.p.length;
		dumpInt(n);


		for (i = 0; i < n; i++)
			dumpFunction(f.p[i], f.source);
	}
	
	void dumpDebug(final Prototype f) throws IOException {
		int i, n;
		n = (strip) ? 0 : f.lineinfo.length;
		dumpInt(n);
		for (i = 0; i < n; i++)
			dumpInt(f.lineinfo[i]);
		n = (strip) ? 0 : f.locvars.length;
		dumpInt(n);
		for (i = 0; i < n; i++) {
			LocVars lvi = f.locvars[i];
			dumpString(lvi.varname);
			dumpInt(lvi.startpc);
			dumpInt(lvi.endpc);
		}
		n = (strip) ? 0 : f.upvalues.length;
		dumpInt(n);
		for (i = 0; i < n; i++)
			dumpString(f.upvalues[i]);
	}
	
	void dumpFunction(final Prototype f, final LuaString string) throws IOException {
		for(String s : opts.getChunkOrder()) {
			//System.out.println("Dumping: " + s);
			if (s.equals("NAME")) {
				if ( f.source == null || f.source.equals(string) || strip )
					dumpInt(0);
				else
					dumpString(f.source);
			}
			if(s.equals("FIRSTL")) {
				dumpInt(f.linedefined);
			}
			if(s.equals("LASTL")) {
				dumpInt(f.lastlinedefined);
			}
			if(s.equals("UPVALS")) {
				dumpChar(f.nups);
			}
			if(s.equals("ARGS")) {
				dumpChar(f.numparams);
			}
			if(s.equals("VARGS")) {
				dumpChar(f.is_vararg);
			}
			if(s.equals("STACK")) {
				dumpChar(f.maxstacksize);
			}
		}

		dumpCode(f);
		dumpConstants(f);
		dumpDebug(f);
		/*
		for(String s : opts.getChunkDataOrder()) {
			System.out.println("[vm] dumping: " + s);
			if(s.equals("CODE")) {
				dumpCode(f);
			}
			if(s.equals("CONSTANTS")) {
				dumpConstants(f);
			}
			if(s.equals("DEBUG")) {
				dumpDebug(f);
			}
		}
		*/


	}


	void dumpHeader() throws IOException {
		writer.write( LUAC_HEADER_SIGNATURE );
		writer.write( LUAC_VERSION );
		writer.write( LUAC_FORMAT );
		writer.write( IS_LITTLE_ENDIAN? 1: 0 );
		writer.write( SIZEOF_INT );
		writer.write( SIZEOF_SIZET );
		writer.write( SIZEOF_INSTRUCTION );
		writer.write( SIZEOF_LUA_NUMBER );
		writer.write( NUMBER_FORMAT );
	}

	/*
	** dump Lua function as precompiled chunk
	*/
	public static int dump( Prototype f, OutputStream w, boolean strip, CompilerOptions opts ) throws IOException {
		AxonDumpState D = new AxonDumpState(w,strip, opts);
		D.dumpHeader();
		D.dumpFunction(f,null);
		return D.status;
	}

	/**
	 * 
	 * @param f the function to dump
	 * @param w the output stream to dump to
	 * @param stripDebug true to strip debugging info, false otherwise
	 * @param numberFormat one of NUMBER_FORMAT_FLOATS_OR_DOUBLES, NUMBER_FORMAT_INTS_ONLY, NUMBER_FORMAT_NUM_PATCH_INT32
	 * @param littleendian true to use little endian for numbers, false for big endian
	 * @return 0 if dump succeeds
	 * @throws IOException
	 * @throws IllegalArgumentException if the number format it not supported
	 */
	public static int dump(Prototype f, OutputStream w, boolean stripDebug, int numberFormat, boolean littleendian, CompilerOptions opts) throws IOException {
		switch ( numberFormat ) {
		case NUMBER_FORMAT_FLOATS_OR_DOUBLES:
		case NUMBER_FORMAT_INTS_ONLY:
		case NUMBER_FORMAT_NUM_PATCH_INT32:
			break;
		default:
			throw new IllegalArgumentException("number format not supported: "+numberFormat);
		}
		AxonDumpState D = new AxonDumpState(w,stripDebug, opts);
		D.IS_LITTLE_ENDIAN = littleendian;
		D.NUMBER_FORMAT = numberFormat;
		D.SIZEOF_LUA_NUMBER = (numberFormat==NUMBER_FORMAT_INTS_ONLY? 4: 8);
		D.dumpHeader();
		D.dumpFunction(f,null);
		return D.status;
	}
}
