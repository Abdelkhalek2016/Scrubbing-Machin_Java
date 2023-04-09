package scrubbingmachine;

import java.io.IOException;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class LutCorrection {
    public LutCorrection() {
        super();
    }
    public LutCorrection(Connection c) {
        conn=c;
    }
    
    boolean isDone=false;
    Connection conn;
    
    
    public boolean startLUTCorrection(String MPN, String se_man_name , String man){
       
        isDone=false;
        StartScrubing ss=new StartScrubing();
        
        
        if(MPN.contains("O")|MPN.contains("o")|MPN.contains("I")|MPN.contains("i")){
            
            String MPNLUT;
            
            MPNLUT = MPN.replace("O", "0");
            MPNLUT = MPNLUT.replace("o", "0");
            Statement stmnt;
            try {
                stmnt = conn.createStatement();
                ResultSet rs = stmnt.executeQuery("select c.com_partnum,m.man_name from cm.xlp_se_component c ,CM.XLP_SE_MANUFACTURER m where c.Nan_Partnum = cm.nonalphanum"+"('"+MPNLUT+"')"+" and M.Man_Name="+"'"+se_man_name+"'"+"and c.man_id=m.man_id");
                while(rs.next()){
                    String com_partnum;
                    String Man_Name;
                    com_partnum = rs.getString(1);
                    Man_Name = rs.getString(2);
                    CorrectedSupplier cs=new CorrectedSupplier();
                    
                        ss.writeIntoOutputFile(MPN + "\t" + se_man_name + "\t" + man + "\t" + "Direct Match LUT" + "\t" +
                                     "Direct Match LUT" + "\t\t\t\t\t\t\t\t" + com_partnum + "\t" + Man_Name + "\t" +
                                     "Lut Correction" + "\t\t\t\t\t\t" + "(CS_daily_Scrubbing)" + "\t" +
                                     "Direct Match LUT" + "\t" + "Scrubbing Machine");
                        isDone=true;
                    
                    
                }
                
            } catch (SQLException e) {
                e.printStackTrace();
            }


        }
    
    return isDone;
    }
}
