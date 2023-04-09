package scrubbingmachine;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;

import java.io.FileReader;

import java.io.FileWriter;

import java.io.IOException;

import java.math.BigDecimal;

import java.sql.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.sql.Statement;

import java.util.HashMap;

import oracle.jdbc.OracleDriver;

public class CorrectedSupplier {
    
    //default constructor
    public CorrectedSupplier() {
        super();         
        }
    // constructor that take connection object
    public CorrectedSupplier(Connection n) {
         conn=n; 

        }     
    
    Connection conn;
    boolean isDone=false;
    private HashMap<String,String> relationtable;
    private HashMap<String,String> commontable;
    File foutput=  new File("Scrubbing_Automation_Output.txt");
    
    
    public void writHeader(){
        FileWriter fw;
        BufferedWriter out;
        try {
            fw = new FileWriter(foutput);
            out=new BufferedWriter(fw);
            System.out.println("MPN\tSE_MAN_NAME\tMFG\tCS_STATUS\tSCRUB_STATUS\tSCRUBBING_STANDARD_COMMENT\tSCRUB_URL_COMMENT_ONLINE\tSCRUB_URL_COMMENT_FILE_PATH\tSCRUB_DS\tSCRUB_DS_FILE_PATH\tPRODUCT_URL PAGE\tTAX_PATH\tSE_PART\tSE_SUPPLIER\tSCRUBBING_ADDITIONAL_COMMENT\tCUSTOMER_DESCRIPTION\tSCRUB_DS_SOURCE\tSCRUB_DS_FILE_TYPE\tSCRUB_URL_COMMENT_SOURCE\tSCRUB_URL_COMMENT_FILE_TYPE\tSOURCING_TASKS_NAME\tTRIGGER_Name\tSCRUBBING_TASK");
            out.write("MPN\tSE_MAN_NAME\tMFG\tCS_STATUS\tSCRUB_STATUS\tSCRUBBING_STANDARD_COMMENT\tSCRUB_URL_COMMENT_ONLINE\tSCRUB_URL_COMMENT_FILE_PATH\tSCRUB_DS\tSCRUB_DS_FILE_PATH\tPRODUCT_URL PAGE\tTAX_PATH\tSE_PART\tSE_SUPPLIER\tSCRUBBING_ADDITIONAL_COMMENT\tCUSTOMER_DESCRIPTION\tSCRUB_DS_SOURCE\tSCRUB_DS_FILE_TYPE\tSCRUB_URL_COMMENT_SOURCE\tSCRUB_URL_COMMENT_FILE_TYPE\tSOURCING_TASKS_NAME\tTRIGGER_Name\tSCRUBBING_TASK");
            out.newLine();
            out.close();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
 
    }

    public boolean startCorrectedSupplier(String MPN, String se_man_name , String man){
        

        isDone=false;
        
        String se_man_name_id="0";       
        String com_id;
        String com_partnum="";
        String man_id;
        String Man_Name="";
        String multisupplier ="";
        StartScrubing ss=new StartScrubing();

        try {

            Statement stmnt = conn.createStatement();            
            
            
            stmnt = conn.createStatement();

            if (se_man_name.equalsIgnoreCase("0")||se_man_name.equalsIgnoreCase("null")||se_man_name.equalsIgnoreCase(null)){
            ResultSet resultsetman= stmnt.executeQuery("select Cross_Man from cm.xlp_se_lookup_man where Given_Man like"+"'"+man+"'");
            if(resultsetman.next()){
                
                se_man_name=resultsetman.getString(1);
                
            }
            resultsetman.close();
            }
            
            ResultSet resultsetse_name_man_id= stmnt.executeQuery("select distinct CM.XLP_SE_MANUFACTURER.Man_id from CM.XLP_SE_MANUFACTURER where Man_Name ="+"'"+ se_man_name+"'");
            System.out.println("after first Select to get se_man_name_id ");
            if(resultsetse_name_man_id.next()){
                
                se_man_name_id=resultsetse_name_man_id.getString(1);
            }
            resultsetse_name_man_id.close();
            System.out.println("se_man_name_id ="+se_man_name_id);
            
            
            ResultSet resultset= stmnt.executeQuery("select distinct com_id,CM.XLP_SE_COMPONENT.COM_PARTNUM,CM.XLP_SE_COMPONENT.man_id,CM.XLP_SE_MANUFACTURER.Man_name from CM.XLP_SE_COMPONENT join CM.XLP_SE_MANUFACTURER on Cm.Xlp_Se_Component.Nan_Partnum = CM.Nonalphanum"+"('"+MPN+"')"+" and CM.XLP_SE_COMPONENT.man_id = CM.XLP_SE_MANUFACTURER.Man_id");
            if(!resultset.isBeforeFirst()){
                isDone=false;
            }
            
            System.out.println("after second select to get all direct rows");
            while(resultset.next()){
                BigDecimal com=(BigDecimal)resultset.getObject(1);
                com_id=com.toString();
                com_partnum=resultset.getString(2);
                BigDecimal mid=(BigDecimal)resultset.getObject(3);
                man_id=mid.toString();
                Man_Name=resultset.getString(4);
                
                System.out.println(com_partnum + Man_Name);
                // Direct Match Exact
                if(se_man_name_id.equalsIgnoreCase(man_id)){
                    
                    System.out.print(MPN+"\t"+se_man_name+"\t"+man+"\t"+"Direct Match"+"\t"+"Direct Match"+"\t\t\t\t\t\t\t\t"+com_partnum+"\t"+Man_Name+"\t\t\t\t\t\t\t"+"(CS_daily_Scrubbing)"+"\t"+"Direct Match"+"\t"+"Scrubbing Machine");
                    System.out.println();
                    
                    ss.writeIntoOutputFile(MPN+"\t"+se_man_name+"\t"+man+"\t"+"Direct Match"+"\t"+"Direct Match"+"\t\t\t\t\t\t\t\t"+com_partnum+"\t"+Man_Name+"\t\t\t\t\t\t\t"+"(CS_daily_Scrubbing)"+"\t"+"Direct Match"+"\t"+"Scrubbing Machine");
                    
                    isDone=true;
                    break;                                                            
                }
                
                //Acquired Realtion (Forwrd and Revrese and incomplete  Acquire Realtion)
                if(relationtable.containsKey(se_man_name_id.concat("concate"+man_id))){
                                    
                                    System.out.print(MPN+"\t"+se_man_name+"\t"+man+"\t"+"Direct Match Corrected supplier"+"\t"+"Direct Match Corrected supplier"+"\t\t\t\t\t\t\t\t"+com_partnum+"\t"+Man_Name+"\t"+relationtable.get(se_man_name_id.concat("concate"+man_id))+"\t\t\t\t\t\t"+"(CS_daily_Scrubbing)"+"\t"+"Direct Match Corrected supplier"+"\t"+"Scrubbing Machine");
                                    System.out.println();
                                    ss.writeIntoOutputFile(MPN+"\t"+se_man_name+"\t"+man+"\t"+"Direct Match Corrected supplier"+"\t"+"Direct Match Corrected supplier"+"\t\t\t\t\t\t\t\t"+com_partnum+"\t"+Man_Name+"\t"+relationtable.get(se_man_name_id.concat("concate"+man_id))+"\t\t\t\t\t\t"+"(CS_daily_Scrubbing)"+"\t"+"Direct Match Corrected supplier"+"\t"+"Scrubbing Machine");
                                    
                                    isDone=true;
                                    break;                
                                }
                
                
                // Common Name for man
                if(commontable.containsKey(Man_Name)&&commontable.get(Man_Name).equalsIgnoreCase(man)){
                    
                    System.out.print(MPN+"\t"+se_man_name+"\t"+man+"\t"+"Direct Match"+"\t"+"Direct Match"+"\t\t\t\t\t\t\t\t"+com_partnum+"\t"+Man_Name+"\t"+"Common name for man,se_name should be mixed"+"\t\t\t\t\t\t"+"(CS_daily_Scrubbing)"+"\t"+"Direct Match"+"\t"+"Scrubbing Machine");
                    System.out.println();
                    
                    ss.writeIntoOutputFile(MPN+"\t"+se_man_name+"\t"+man+"\t"+"Direct Match"+"\t"+"Direct Match"+"\t\t\t\t\t\t\t\t"+com_partnum+"\t"+Man_Name+"\t"+"Common name for man,se_name should be mixed"+"\t\t\t\t\t\t"+"(CS_daily_Scrubbing)"+"\t"+"Direct Match"+"\t"+"Scrubbing Machine");
                    
                    isDone=true;
                    break;         
                }
                
                // if se_name blank or 0 or contain"|",and man equal common man
                
             if((commontable.get(Man_Name)!=null)){
                 
                if((se_man_name.equalsIgnoreCase("0")||se_man_name.contains("|"))&&commontable.get(Man_Name).equalsIgnoreCase(man)){
                    System.out.println("entered condition");
                    System.out.print(MPN+"\t"+se_man_name+"\t"+man+"\t"+"Direct Match"+"\t"+"Direct Match"+"\t\t\t\t\t\t\t\t"+com_partnum+"\t"+Man_Name+"\t"+"Common name for man,se_name should be mixed"+"\t\t\t\t\t\t"+"(CS_daily_Scrubbing)"+"\t"+"Direct Match"+"\t"+"Scrubbing Machine");
                    System.out.println();
                    
                    ss.writeIntoOutputFile(MPN+"\t"+se_man_name+"\t"+man+"\t"+"Direct Match"+"\t"+"Direct Match"+"\t\t\t\t\t\t\t\t"+com_partnum+"\t"+Man_Name+"\t"+"Common name for man,se_name should be mixed"+"\t\t\t\t\t\t"+"(CS_daily_Scrubbing)"+"\t"+"Direct Match"+"\t"+"Scrubbing Machine");
                    
                    isDone=true;
                    break;  
                
                }
             }
                
                
                //if man blank(0) and se_man_name blank(0)  "Diect Match Multi
               
                  if((man.equalsIgnoreCase("null")||man.equalsIgnoreCase("N/A")||man.equalsIgnoreCase("0")||man.equalsIgnoreCase("any"))&&(se_man_name.equalsIgnoreCase("0")||se_man_name.equalsIgnoreCase("")||se_man_name.equalsIgnoreCase("any"))){
                    
                    multisupplier=Man_Name.concat("|"+multisupplier);
                    
                    
                }
                
            }
            System.out.println("out of While loop of resultset");
            if(multisupplier.length()!=0){
                ss.writeIntoOutputFile(MPN+"\t"+se_man_name+"\t"+man+"\t"+"Direct Match corrected supplier"+"\t"+"Direct Match corrected supplier"+"\t\t\t\t\t\t\t\t"+com_partnum+"\t"+multisupplier+"\t"+"Blank Man or Dummy Man"+"\t\t\t\t\t\t"+"(CS_daily_Scrubbing)"+"\t"+"Direct Match corrected supplier"+"\t"+"Scrubbing Machine");
                
                isDone=true;
                
            }
            
            resultset.close();
            stmnt.close();
            
        
        } catch (SQLException e) {
           e.printStackTrace();
        } 
        
        return isDone;
    }

    public void setRelationtable() {
        HashMap<String, String> relationtable=new HashMap<String, String>() ;
        try{
        Statement stmnt= conn.createStatement();
                    
                    ResultSet resultsetMFRrelation= stmnt.executeQuery("select * from MFR_Relation");
                        while(resultsetMFRrelation.next()) { 
                            relationtable.put(resultsetMFRrelation.getString(6),"Forward_Relation");
                            relationtable.put(resultsetMFRrelation.getString(7),"Reverse_Relation");
                        }
                        resultsetMFRrelation.close();
                        stmnt.close();
            
        }catch(Exception e){
            e.printStackTrace();
        }
        
        
        this.relationtable = relationtable;
    }

    public HashMap<String, String> getRelationtable() {
        return relationtable;
    }

    public void setCommon() {
        HashMap<String, String> commontable=new HashMap<String, String>() ;
        try{
        Statement stmnt= conn.createStatement();
                    
                    ResultSet resultsetCommon= stmnt.executeQuery("select * from Common");
                        while(resultsetCommon.next()) { 
                            commontable.put(resultsetCommon.getString(2),resultsetCommon.getString(1));
                        }
                        resultsetCommon.close();
                        stmnt.close();
            
        }catch(Exception e){
            e.printStackTrace();
        }
        
        
        
        this.commontable = commontable;
    }

    public HashMap<String, String> getCommon() {
        return commontable;
    }
}
