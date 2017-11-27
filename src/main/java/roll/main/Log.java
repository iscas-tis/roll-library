/* Copyright (c) 2016, 2017                                               */
/*       Institute of Software, Chinese Academy of Sciences               */
/* This file is part of ROLL, a Regular Omega Language Learning library.  */
/* ROLL is free software: you can redistribute it and/or modify           */
/* it under the terms of the GNU General Public License as published by   */
/* the Free Software Foundation, either version 3 of the License, or      */
/* (at your option) any later version.                                    */

/* This program is distributed in the hope that it will be useful,        */
/* but WITHOUT ANY WARRANTY; without even the implied warranty of         */
/* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the          */
/* GNU General Public License for more details.                           */

/* You should have received a copy of the GNU General Public License      */
/* along with this program.  If not, see <http://www.gnu.org/licenses/>.  */

package roll.main;

import java.io.OutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Yong Li (liyong@ios.ac.cn)
 * */

public class Log {
    
    private final PrintStream log;
    private final String DEBUG = "debug";
    private final String INFO = "info";
    private final String ERROR = "error";
    private final Options options;
    
    public Log(Options options, PrintStream out) {
        assert options != null && out != null;
        this.options = options;
        this.log = out;
    }
    
    public void debug(String content) {
        this.write(DEBUG, content);
    }
    
    public void verbose(String content) {
        if(options.verbose) {
            log.println(content);
        }
    }
    
    public void println(String content) {
        log.println(content);
    }
    
    public void print(String content) {
        log.print(content);
    }
    
    public void info(String content) {
        this.write(INFO, content);
    }
    
    public void err(String content) {
        this.write(ERROR, content);
    }
    
    public OutputStream getOutputStream() {
        return log;
    }
    
    private void write(String mode, String content) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        PrintStream out = log;
        if (mode == ERROR) out = System.err;
        out.printf("%s [%5s] %s\n", dateFormat.format(new Date()), mode, content);
    }

}
