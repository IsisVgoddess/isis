/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

/**
 * 
 */
package org.apache.isis.runtimes.dflt.objectstores.sql.common;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;

import junit.framework.TestCase;

import org.apache.isis.applib.value.Color;
import org.apache.isis.applib.value.Date;
import org.apache.isis.applib.value.DateTime;
import org.apache.isis.applib.value.Image;
import org.apache.isis.applib.value.Password;
import org.apache.isis.applib.value.Percentage;
import org.apache.isis.applib.value.Time;
import org.apache.isis.applib.value.TimeStamp;
import org.apache.isis.core.commons.config.IsisConfigurationBuilderFileSystem;
import org.apache.isis.runtimes.dflt.objectstores.sql.singleton.SqlIntegrationTestSingleton;
import org.apache.isis.runtimes.dflt.objectstores.sql.testsystem.SqlDataClassFactory;
import org.apache.isis.runtimes.dflt.objectstores.sql.testsystem.dataclasses.SimpleClass;
import org.apache.isis.runtimes.dflt.objectstores.sql.testsystem.dataclasses.SimpleClassTwo;
import org.apache.isis.runtimes.dflt.objectstores.sql.testsystem.dataclasses.SqlDataClass;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * @author Kevin kevin@kmz.co.za
 * 
 *         The Singleton class {@link SqlIntegrationTestSingleton} is used to
 *         preserve values between tests. If {@link SqlIntegrationTestSingleton}
 *         state is 0, then a full NOF context is recreated. If
 *         {@link SqlIntegrationTestSingleton} state is 1, then the previous
 *         context is re-used.
 * 
 *         The state of 1 is used to separate tests into stand-alone methods,
 *         for clarity purposes - without reloading the entire framework.
 * 
 */
public abstract class SqlIntegrationTestCommon extends TestCase {
    private static final Logger LOG = Logger.getLogger(SqlIntegrationTestCommon.class);
    
    
    private static final TimeZone UTC_TIME_ZONE;
    // Helper values
    private static final java.sql.Date sqlDate;// = java.sql.Date.valueOf("2010-03-05");
    
    static {
        TimeZone timeZone = TimeZone.getTimeZone("Etc/UTC");
        if (timeZone == null) {
            timeZone = TimeZone.getTimeZone("UTC");
        }
        UTC_TIME_ZONE = timeZone;
        
        final Calendar cal = Calendar.getInstance();
        cal.setTimeZone(UTC_TIME_ZONE);
        cal.set(2011, 4-1, 8, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        
        sqlDate = new java.sql.Date(cal.getTimeInMillis());
    }
	
	private static final Date applibDate = new Date(2010, 3, 5);
	private static final DateTime dateTime = new DateTime(2010, 3, 5, 1, 23);
	private static final TimeStamp timeStamp = new TimeStamp(
			dateTime.longValue());
	private static final Time time = new Time(14, 56);
	
	private static final Color color = Color.BLACK;
	private static final Image image = new Image(new int[][] { { 1, 2, 3 },
			{ 4, 5, 6 }, { 7, 8, 9 } });
	private static final Password password = new Password("password");
	private static final Percentage percentage = new Percentage(42);
    // private static final Money money = new Money(99.99, "GBP");
	
	// Collection mapper tests
	private static final List<String> stringList1 = Arrays.asList("Baking",
			"Bakery", "Canned", "Dairy");
	private static final List<String> stringList2 = Arrays.asList("Fridge",
			"Deli", "Fresh Produce", "Frozen", "Household", "Other..");
	private static List<SimpleClass> simpleClassList1 = new ArrayList<SimpleClass>();
	private static List<SimpleClass> simpleClassList2 = new ArrayList<SimpleClass>();

	private static SimpleClassTwo simpleClassTwoA;
	// private static SimpleClassTwo simpleClassTwoB;

	IsisConfigurationBuilderFileSystem loader;

	private SqlIntegrationTestSingleton getSingletonInstance() {
		return SqlIntegrationTestSingleton.getInstance();
	}

	public Properties getProperties() {
		try {
			Properties properties = new Properties();
			properties.load(new FileInputStream("src/test/config/"
					+ getPropertiesFilename()));
			return properties;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	public abstract String getPropertiesFilename();

	public String getPersonTableName() {
		return "sqldataclass";
	}

	public String getSimpleClassTableName() {
		return "simpleclass";
	}

	public String getSimpleClassTwoTableName() {
		return "simpleclasstwo";
	}

	public String getSqlSetupString() {
		return null;
	}

	public String getSqlTeardownString() {
		return null;
	}

	/**
	 * This method can be used to do any DB specific actions the first time the
	 * test framework is setup. e.g. In the XML test, it must delete all XML
	 * files in the data store directory.
	 */
	public void initialiseTests() {
	}

	// Set-up the test environment
	@Override
	public void setUp() throws FileNotFoundException, IOException,
			ClassNotFoundException, InstantiationException,
			IllegalAccessException, SQLException {
		Logger.getRootLogger().setLevel(Level.INFO);

		// Initialise the framework
		if (getSingletonInstance().getState() == 0) {
			Properties properties = getProperties();
			if (properties == null) {
				getSingletonInstance().initNOF("src/test/config",
						getPropertiesFilename());
			} else {
				getSingletonInstance().initNOF(properties);
			}

			String sqlSetupString = getSqlSetupString();
			if (sqlSetupString != null) {
				getSingletonInstance().sqlExecute(sqlSetupString);
			}
		}
	}

	// Tear down the test environment
	@Override
	public void tearDown() {
		if (getSingletonInstance().getState() == 0) {
			String sqlTeardownString = getSqlTeardownString();
			if (sqlTeardownString != null) {
				try {
					getSingletonInstance().sqlExecute(sqlTeardownString);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			getSingletonInstance().shutDown();
		}
	}

	/**
	 * TODO Confirm that the system tables are created as expected
	 */
	public void testSetup() {
		initialiseTests();
		getSingletonInstance().setState(0);
	}

	/**
	 * Create a {@link SqlDataClass} and persist to the store.
	 * 
	 * @throws Exception
	 */
	public void testCreate() throws Exception {
		SqlIntegrationTestSingleton.drop(getPersonTableName());
		SqlIntegrationTestSingleton.drop(getSimpleClassTableName());
		SqlIntegrationTestSingleton.drop(getSimpleClassTwoTableName());

		SqlDataClassFactory factory = SqlIntegrationTestSingleton
				.getSqlDataClassFactory();
		SqlDataClass sqlDataClass = factory.newDataClass();
		sqlDataClass.setString("Test String");
		sqlDataClass.setDate(applibDate);
		sqlDataClass.setSqlDate(sqlDate);
		// sqlDataClass.setMoney(money); // TODO: Money is broken
		sqlDataClass.setDateTime(dateTime);
		sqlDataClass.setTimeStamp(timeStamp);
		sqlDataClass.setTime(time);
		sqlDataClass.setColor(color);
		sqlDataClass.setImage(image);
		sqlDataClass.setPassword(password);
		sqlDataClass.setPercentage(percentage);

		// Setup SimpleClassTwo
		simpleClassTwoA = factory.newSimpleClassTwo();
		simpleClassTwoA.setText("A");
		// simpleClassTwoB = factory.newSimpleClassTwo();
		// simpleClassTwoB.setString("A");

		sqlDataClass.setSimpleClassTwo(simpleClassTwoA);

		// Initialise collection1
		boolean bMustAdd = false;
		if (simpleClassList1.size() == 0) {
			bMustAdd = true;
		}
		for (String string : SqlIntegrationTestCommon.stringList1) {
			SimpleClass simpleClass = factory.newSimpleClass();
			simpleClass.setString(string);
			simpleClass.setSimpleClassTwoA(simpleClassTwoA);
			sqlDataClass.addToSimpleClasses1(simpleClass);
			if (bMustAdd) {
				simpleClassList1.add(simpleClass);
			}
		}

		// Initialise collection2
		/**/
		for (String string : SqlIntegrationTestCommon.stringList2) {
			SimpleClass simpleClass = factory.newSimpleClass();
			simpleClass.setString(string);
			simpleClass.setSimpleClassTwoA(simpleClassTwoA);
			sqlDataClass.addToSimpleClasses2(simpleClass);
			if (bMustAdd) {
				simpleClassList2.add(simpleClass);
			}
		}
		/**/
		factory.save(sqlDataClass);
		
		// For in-memory only!
		if (getProperties().getProperty("isis.persistor") == "in-memory") {
			getSingletonInstance().setState(1);
		}

	}

	/**
	 * Test loading a persisted {@link SqlDataClass} from the sql store.
	 * 
	 * @throws Exception
	 */
	public void testLoad() throws Exception {
		SqlDataClassFactory factory = SqlIntegrationTestSingleton
				.getSqlDataClassFactory();
		List<SqlDataClass> people = factory.allDataClasses();
		assertEquals(1, people.size());
		SqlDataClass sqlDataClass = people.get(0);
		SqlIntegrationTestSingleton.setPerson(sqlDataClass);
		getSingletonInstance().setState(1);
	}

	public void testSimpleClassCollection1Lazy() {
		SqlDataClass sqlDataClass = SqlIntegrationTestSingleton.getPerson();
		List<SimpleClass> collection = sqlDataClass.simpleClasses1;// getSimpleClasses1();

		assertEquals("collection size is not equal!", collection.size(),
				simpleClassList1.size());
		/*
		 * if (collection.size() == 0) { SqlDataClassFactory factory =
		 * SqlIntegrationTestSingleton .getSqlDataClassFactory();
		 * factory.resolve(sqlDataClass); }
		 */
	}

	/**
	 * Test {@link SqlDataClass} {@link String} field.
	 * 
	 * @throws Exception
	 */
	public void testString() {
		SqlDataClass sqlDataClass = SqlIntegrationTestSingleton.getPerson();
		assertEquals("Test String", sqlDataClass.getString());
	}

	/**
	 * Test {@link SqlDataClass} {@link Date} field.
	 * 
	 * @throws Exception
	 */
	public void testDate() {
		SqlDataClass sqlDataClass = SqlIntegrationTestSingleton.getPerson();
		
        LOG.log(Level.INFO, "Test: testDate()");
        
        LOG.log(Level.INFO, "sqlDataClass.getDate() as String: "+sqlDataClass.getDate());
        LOG.log(Level.INFO, "applibDate.dateValue() as String: "+applibDate);
        
        LOG.log(Level.INFO, "sqlDataClass.getDate().getTime() as Long: "+sqlDataClass.getDate().dateValue().getTime());
        LOG.log(Level.INFO, "applibDate.dateValue() as Long: "+applibDate.dateValue().getTime());
        
        if (!applibDate.isEqualTo(sqlDataClass.getDate())){
            fail(applibDate.toString() + " is not equal to "
                + sqlDataClass.getDate().toString());
        }
		
	}

	/**
	 * Test {@link SqlDataClass} {@link java.sql.Date} field.
	 * 
	 * @throws Exception
	 */
	public void testSqlDate() {
		SqlDataClass sqlDataClass = SqlIntegrationTestSingleton.getPerson();
		
		LOG.log(Level.INFO, "Test: testSqlDate()");
		LOG.log(Level.INFO, "sqlDataClass.getSqlDate() as String:"+sqlDataClass.getSqlDate());
        LOG.log(Level.INFO, "sqlDate.toString() as String:"+sqlDate);
		
        LOG.log(Level.INFO, "sqlDataClass.getSqlDate().getTime() as Long:"+sqlDataClass.getSqlDate().getTime());
		LOG.log(Level.INFO, "sqlDate.getTime() as Long:"+sqlDate.getTime());
        
        if (sqlDate.compareTo(sqlDataClass.getSqlDate()) != 0){
            fail("SQL date " + sqlDate.toString() + " is not equal to "
                + sqlDataClass.getSqlDate().toString());
        }
		
	}

    public void testDateTimezoneIssue() {
        /*
         * At the moment, applib Date and java.sql.Date are restored from ValueSemanticsProviderAbstractTemporal with
         * an explicit hourly offset that comes from the timezone. I.e. in South Africa, with TZ +2h00, they have an
         * implicit time of 02h00 (2AM). This can potentially seriously screw up GMT-X dates, which, I suspect, will
         * actually be set to the dat BEFORE.
         * 
         * This test is a simple test to confirm that date/time before and after checks work as expected.   
         *  
         */
        SqlDataClass sqlDataClass = SqlIntegrationTestSingleton.getPerson();
        
        DateTime dateTime = sqlDataClass.getDateTime(); // new DateTime(2010, 3, 5, 1, 23);
        Date date = sqlDataClass.getDate(); // new Date(2010, 3, 5);
        java.sql.Date sqlDate = sqlDataClass.getSqlDate(); // "2010-03-05"

        /**/
        //assertTrue("dateTime's value ("+dateTime.dateValue()+
        //    ") should be after java.sql.date's ("+ date +")", dateTime.dateValue().after(sqlDate));

        //assertTrue("dateTime's value ("+dateTime.dateValue()+
        //    ") should be after date's ("+ date +")", dateTime.dateValue().after(date.dateValue()));
        /**/
    }
	
	/**
	 * Test {@link Money} type.
	 */
	/*
	 * public void testMoney(){ SqlDataClass sqlDataClass =
	 * SqlIntegrationTestSingleton.getPerson();
	 * assertTrue("Money "+money.toString
	 * ()+" is not equal to "+sqlDataClass.getMoney().toString(),
	 * money.equals(sqlDataClass.getMoney())); }
	 */

	/**
	 * Test {@link DateTime} type.
	 */
	public void testDateTime() {
		SqlDataClass sqlDataClass = SqlIntegrationTestSingleton.getPerson();
		
		
        LOG.log(Level.INFO, "Test: testDateTime()");
        LOG.log(Level.INFO, "sqlDataClass.getDateTime() as String:"+sqlDataClass.getDateTime());
        LOG.log(Level.INFO, "dateTime.toString() as String:"+dateTime);
        
        LOG.log(Level.INFO, "sqlDataClass.getDateTime().getTime() as Long:"+sqlDataClass.getDateTime().longValue());
        LOG.log(Level.INFO, "dateTime.getTime() as Long:"+dateTime.longValue());
		
        if (!dateTime.equals(sqlDataClass.getDateTime())){
            fail("DateTime " + dateTime.toString() + " is not equal to "
                + sqlDataClass.getDateTime().toString());
        }
	}

	/**
	 * Test {@link TimeStamp} type.
	 */
	public void testTimeStamp() {
		SqlDataClass sqlDataClass = SqlIntegrationTestSingleton.getPerson();
		assertTrue("TimeStamp " + timeStamp.toString() + " is not equal to "
				+ sqlDataClass.getTimeStamp().toString(),
				timeStamp.isEqualTo(sqlDataClass.getTimeStamp()));
	}

	/**
	 * Test {@link Time} type.
	 */
	/**/
	public void testTime() {
		SqlDataClass sqlDataClass = SqlIntegrationTestSingleton.getPerson();
		assertNotNull("sqlDataClass is null", sqlDataClass);
		assertNotNull("getTime() is null", sqlDataClass.getTime());
		assertTrue("Time " + sqlDataClass.getTime().toString()
				+ " is not equal to " + time.toString(),
				time.isEqualTo(sqlDataClass.getTime()));
	}

	/**/

	/**
	 * Test {@link Color} type.
	 */
	public void testColor() {
		SqlDataClass sqlDataClass = SqlIntegrationTestSingleton.getPerson();
		assertTrue("Color " + color.toString() + " is not equal to "
				+ sqlDataClass.getColor().toString(),
				color.isEqualTo(sqlDataClass.getColor()));
	}

	/**
	 * Test {@link Image} type.
	 */
	// TODO: Images are not equal...
	/*
	 * public void testImage(){ SqlDataClass sqlDataClass =
	 * SqlIntegrationTestSingleton.getPerson(); Image image2 =
	 * sqlDataClass.getImage(); assertEqual(image, image2); }
	 * 
	 * private void assertEqual(Image image2, Image image3) {
	 * assertEquals(image2.getHeight(), image3.getHeight());
	 * assertEquals(image2.getWidth(), image3.getWidth()); boolean same = true;
	 * int i=0,j=0; int p1=0, p2=0; String error = ""; int [][] i1 =
	 * image2.getImage(), i2 = image3.getImage(); for(i = 0; same &&
	 * i<image2.getHeight();i++){ int [] r1 = i1[i], r2 = i2[i]; for (j = 0;
	 * same && j < image2.getWidth(); j++){ if (r1[j] != r2[j]){ same = false;
	 * p1 = r1[j]; p2 = r2[j]; error =
	 * "Images differ at i = "+i+", j = "+j+", "+p1+ " is not "+p2+"!"; break; }
	 * } } assertTrue(error, same); }
	 */

	/**
	 * Test {@link Password} type.
	 */
	public void testPassword() {
		SqlDataClass sqlDataClass = SqlIntegrationTestSingleton.getPerson();
		assertTrue("Password " + password.toString() + " is not equal to "
				+ sqlDataClass.getPassword().toString(),
				password.equals(sqlDataClass.getPassword()));
	}

	/**
	 * Test {@link Percentage} type.
	 */
	public void testPercentage() {
		SqlDataClass sqlDataClass = SqlIntegrationTestSingleton.getPerson();
		assertTrue("Percentage " + percentage.toString() + " is not equal to "
				+ sqlDataClass.getPercentage().toString(),
				percentage.equals(sqlDataClass.getPercentage()));
	}

	/**
	 * Test {@link StringCollection} type.
	 */
	/*
	 * public void testStringCollection(){ SqlDataClass sqlDataClass =
	 * SqlIntegrationTestSingleton.getPerson(); List<String> collection =
	 * sqlDataClass.getStringCollection(); int i = 0; for (String string :
	 * collection) { assertEquals(SqlIntegrationTestCommon.stringList.get(i++),
	 * string); } }
	 */

	public void testSingleReferenceLazy() {
		SqlDataClass sqlDataClass = SqlIntegrationTestSingleton.getPerson();
		SimpleClassTwo a = sqlDataClass.getSimpleClassTwo();
		if (getProperties().getProperty("isis.persistor") != "in-memory") {
			assertEquals(null, a.text); // must check direct value, as
			// framework can auto-resolve, if you use getText()
		}
	}

	/**
	 * Test a collection of {@link SimpleClass} type.
	 */
	public void testSimpleClassCollection1() {
		SqlDataClass sqlDataClass = SqlIntegrationTestSingleton.getPerson();
		List<SimpleClass> collection = sqlDataClass.getSimpleClasses1();

		assertEquals("collection size is not equal!",
				SqlIntegrationTestCommon.simpleClassList1.size(),
				collection.size());

		int i = 0;
		for (SimpleClass simpleClass : SqlIntegrationTestCommon.simpleClassList1) {
			assertEquals(simpleClass.getString(), collection.get(i++)
					.getString());
		}
	}

	/**
	 * Test another collection of {@link SimpleClass} type.
	 */
	/**/
	public void testSimpleClassCollection2() {
		SqlDataClass sqlDataClass = SqlIntegrationTestSingleton.getPerson();
		List<SimpleClass> collection = sqlDataClass.getSimpleClasses2();

		assertEquals("collection size is not equal!",
				SqlIntegrationTestCommon.simpleClassList2.size(),
				collection.size());

		int i = 0;
		for (SimpleClass simpleClass : SqlIntegrationTestCommon.simpleClassList2) {
			assertEquals(simpleClass.getString(), collection.get(i++)
					.getString());
		}
	}

	public void testSimpleClassTwoReferenceLazy() {
		SqlDataClass sqlDataClass = SqlIntegrationTestSingleton.getPerson();
		List<SimpleClass> collection = sqlDataClass.getSimpleClasses1();
		if (getProperties().getProperty("isis.persistor") != "in-memory") {
			for (SimpleClass simpleClass : collection) {
				SimpleClassTwo a = simpleClass.getSimpleClassTwoA();
				assertEquals(null, a.text); // must check direct value, as
											// framework can auto-resolve, if
											// you use getText()
			}
		}
	}

	public void testSingleReferenceResolve() {
		SqlDataClassFactory factory = SqlIntegrationTestSingleton
				.getSqlDataClassFactory();
		SqlDataClass sqlDataClass = SqlIntegrationTestSingleton.getPerson();
		SimpleClassTwo a = sqlDataClass.getSimpleClassTwo();
		factory.resolve(a);
		assertEquals(simpleClassTwoA.getText(), a.getText());
	}

	public void testSimpleClassTwoReferenceResolve() {
		SqlDataClassFactory factory = SqlIntegrationTestSingleton
				.getSqlDataClassFactory();
		SqlDataClass sqlDataClass = SqlIntegrationTestSingleton.getPerson();
		List<SimpleClass> collection = sqlDataClass.getSimpleClasses1();
		for (SimpleClass simpleClass : collection) {
			SimpleClassTwo a = simpleClass.getSimpleClassTwoA();
			factory.resolve(a);
			assertEquals(simpleClassTwoA.getText(), a.getText());
		}
	}

	public void testSimpleClassTwo() {
		SqlDataClassFactory factory = SqlIntegrationTestSingleton
				.getSqlDataClassFactory();
		List<SimpleClassTwo> classes = factory.allSimpleClassTwos();
		assertEquals(1, classes.size());
		for (SimpleClassTwo simpleClass : classes) {
			assertEquals(simpleClassTwoA.getText(), simpleClass.getText());
		}
	}

	public void testUpdate1() {
		SqlDataClassFactory factory = SqlIntegrationTestSingleton
				.getSqlDataClassFactory();
		List<SimpleClassTwo> classes = factory.allSimpleClassTwos();
		assertEquals(1, classes.size());

		SimpleClassTwo simpleClass = classes.get(0);
		simpleClass.setText("XXX");
	}

	public void testUpdate2() {
		SqlDataClassFactory factory = SqlIntegrationTestSingleton
				.getSqlDataClassFactory();
		List<SimpleClassTwo> classes = factory.allSimpleClassTwos();
		assertEquals(1, classes.size());

		SimpleClassTwo simpleClass = classes.get(0);
		assertEquals("XXX", simpleClass.getText());
	}

	// Last "test" - Set the Singleton state to 0 to invoke a clean shutdown.
	public void testSetStateZero() {
		getSingletonInstance().setState(0);
	}

}
