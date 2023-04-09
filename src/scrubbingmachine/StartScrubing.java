package scrubbingmachine;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;

import java.io.FileReader;

import java.io.FileWriter;

import java.io.IOException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import java.util.concurrent.TimeUnit;

import oracle.jdbc.OracleDriver;

public class StartScrubing {
    public StartScrubing() {
        super();
    }
    
    boolean isMainDone;
    File foutput=  new File("Scrubbing_Automation_Output.txt");
    FileWriter fw;
    BufferedWriter out;
    
    public static Connection getConnection() throws SQLException {
            String username = "Read_Only";
            String password = "Read_Only";
            String thinConnURL = "jdbc:oracle:thin:@10.0.2.12:1521:XLP";
            DriverManager.registerDriver(new OracleDriver());
            Connection connection = DriverManager.getConnection(thinConnURL, username, password);
            connection.setAutoCommit(false);
            System.out.println("Connection Done");
            return connection;
    }
    
    //Method to Write into output file
    public void writeIntoOutputFile(String s){
        
       
        try {
            fw = new FileWriter(foutput, true);
            out=new BufferedWriter(fw);
            out.write(s);
            out.newLine();
            out.flush();
            out.close();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }
    
    

    public static void main(String[] args) {
        
        long startTime = System.nanoTime();
        
        File finput=new File("test3.txt");
        StartScrubing ss=new StartScrubing();
        
        
        try{
        CorrectedSupplier cs=new CorrectedSupplier(StartScrubing.getConnection());
        LutCorrection lut=new LutCorrection(cs.conn);
        cs.setRelationtable();
        cs.setCommon();    
        cs.writHeader();
        
        
            FileReader fir=new FileReader(finput);
            BufferedReader br=new BufferedReader(fir);
            
            
            String MPN="0";
            String se_man_name="0";
            String man="0";
            String line =br.readLine();
            line=br.readLine();
            while(line!=null){
                System.out.println("enter input file lines");
                        String[] row=line.split("\t");
                            MPN=row[0];
                            se_man_name=row[1];
                            man=row[2];
                            line=br.readLine();
                System.out.println(MPN+se_man_name+man);
                cs.startCorrectedSupplier(MPN, se_man_name, man);
                System.out.println(cs.isDone);
                /*
                if(!cs.isDone){
                lut.startLUTCorrection(MPN, se_man_name, man);
                }
                */
                
                if(!cs.isDone){   
                    ss.writeIntoOutputFile(MPN+"\t"+se_man_name+"\t"+man+"\t"+"Need Manual Scrubbing"+"\t"+"Need Manual Scrubbing"+"\t\t\t\t\t\t\t\t"+"\t"+"\t\t\t\t\t\t\t"+"(CS_daily_Scrubbing)"+"\t"+"Need Manual Scrubbing"+"\t"+"Scrubbing Machine");
                    
            }
                
            }
           
        }catch(Exception e){
            e.printStackTrace();
        } 
        
        long endTime   = System.nanoTime();
        long totalTime = endTime - startTime;     
        System.out.println("Total Time to Finish Scrubbing is: "+TimeUnit.NANOSECONDS.toSeconds(totalTime)+" Seconds");
    }
}
