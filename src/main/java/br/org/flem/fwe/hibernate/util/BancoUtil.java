/*
 * GeraBanco.java
 *
 * Created on 25/07/2007, 08:14:21
 *
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package br.org.flem.fwe.hibernate.util;
 
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.cfg.Configuration;
import org.hibernate.tool.hbm2ddl.SchemaExport;

/**
 *
 * @author mjpereira
 */
public class BancoUtil {
    
    public BancoUtil() {
    }
    
    public static void geraBanco() {
        Configuration conf = new AnnotationConfiguration();
        conf.configure();
        SchemaExport se = new SchemaExport(conf);
        se.create(true, true);
     }

}
