/**
 * Realisation de quelques tests
 */
package com.bootcampWS.rest.tests;

import java.util.Properties;
import javax.persistence.Persistence;
import org.testng.annotations.Test;

/**
 *
 * @author soul
 */
public class GenerateTableTest  {

  @Test
    public void generateTables(){

        Persistence.createEntityManagerFactory("WSProgrammePU", new Properties() {});
    }
}
