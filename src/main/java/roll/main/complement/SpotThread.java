package roll.main.complement;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;

import roll.automata.NBA;
import roll.automata.operations.nba.inclusion.NBAInclusionCheckTool;

public class SpotThread extends Thread {
	Boolean result = null;
	
	NBA spotBFC;
	NBA spotB;
	
	Process process;
	
	boolean flag;
	
	public SpotThread(NBA BFC, NBA B) {
		this.spotBFC = BFC;
		this.spotB = B;
	}
	
	public Boolean getResult() {
		return result;
	}
	
	@Override
	public void run() {
		String command = "autfilt --included-in=";
		File fileA = new File("/tmp/A.hoa");
        File fileB = new File("/tmp/B.hoa");
        try {
        	NBAInclusionCheckTool.outputHOAStream(spotB, new PrintStream(new FileOutputStream(fileA)));
        	NBAInclusionCheckTool.outputHOAStream(spotBFC, new PrintStream(new FileOutputStream(fileB)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        final Runtime rt = Runtime.getRuntime();
        // check whether it is included in A.hoa
        command = command + fileA.getAbsolutePath() + " " + fileB.getAbsolutePath();
        System.out.println(command);
        flag = true;
        process = null;
        try {
        	process = rt.exec(command);
        	while(process.isAlive() && flag) {
        		// do nothing here
        	}
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        final BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line = null;
        try {
            while (flag && (line = reader.readLine()) != null ) {
                if (line.contains("HOA")) {
                    result = true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	
	@Override
	public void interrupt() {
		if(process != null) {
			flag = false;
			process.destroyForcibly();
		}
		super.interrupt();
	}
}
