/**
 *
 * @author Wei-Ming Wu
 *
 *
 * Copyright 2013 Wei-Ming Wu
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */
package com.github.wnameless.workbookaccessor;

import static net.sf.rubycollect4j.RubyCollections.Hash;
import static net.sf.rubycollect4j.RubyCollections.ra;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import net.sf.rubycollect4j.RubyArray;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.google.common.testing.NullPointerTester;

public class WorkbookReaderTest {

  private static final String BASE_DIR = "src/test/resources/";

  private WorkbookReader reader;
  private WorkbookReader readerNH;
  private RubyArray<String> header;
  private RubyArray<String> firstLine;
  private RubyArray<String> firstLineCSV;

  @Rule
  public ExpectedException expectedEx = ExpectedException.none();

  @Before
  public void setUp() throws Exception {
    reader = new WorkbookReader(BASE_DIR + "PII_20130328154417.xls");
    readerNH = new WorkbookReader(BASE_DIR + "PII_20130328154417.xls", false);
    header =
        ra("編碼日期", "GUID", "MRN", "身份證字號", "姓氏", "名字", "出生月", "出生日", "出生年",
            "聯絡電話", "性別", "收案醫師", "收案醫院名稱");
    firstLine =
        ra("2013/03/28", "BIS-KJ415MTP", "A123456", "A286640890", "黃", "小宜",
            "10", "19", "1979", "TEL0910,123,456", "", "李大華", "北榮");
    firstLineCSV =
        ra("2013/03/28", "BIS-KJ415MTP", "A123456", "A286640890", "黃", "小宜",
            "10", "19", "1979", "\"TEL0910,123,456\"", "", "李大華", "北榮");
  }

  @Test
  public void testNullproof() {
    expectedEx.expect(NullPointerException.class);
    expectedEx.expectMessage("Parameter<String> is not nullable");
    new WorkbookReader((String) null);
  }

  @Test
  public void testAllConstructorsNPE() {
    new NullPointerTester().testAllPublicConstructors(WorkbookReader.class);
  }

  @Test
  public void testAllPublicMethodsNPE() {
    new NullPointerTester().testAllPublicInstanceMethods(reader);
  }

  @Test
  public void testConstructor() {
    assertTrue(reader instanceof WorkbookReader);
    assertTrue(readerNH instanceof WorkbookReader);
    assertTrue(new WorkbookReader(new File(BASE_DIR + "PII_20130328154417.xls")) instanceof WorkbookReader);
    assertTrue(new WorkbookReader(
        new File(BASE_DIR + "PII_20130328154417.xls"), false) instanceof WorkbookReader);
    assertTrue(new WorkbookReader(BASE_DIR + "PII_20130328154417.xls", false) instanceof WorkbookReader);
    Workbook wb = new HSSFWorkbook();
    wb.createSheet();
    assertTrue(new WorkbookReader(wb) instanceof WorkbookReader);
    assertTrue(new WorkbookReader(wb, false) instanceof WorkbookReader);
  }

  @Test(expected = RuntimeException.class)
  public void testConstructorException() {
    new WorkbookReader("no_file.xls");
  }

  @Test
  public void testGetWorkbook() {
    assertTrue(reader.getWorkbook() instanceof Workbook);
  }

  @Test
  public void testGetHeader() {
    assertEquals(header, reader.getHeader());
    assertEquals(ra(), readerNH.getHeader());
  }

  @Test
  public void testGetHeaderException() throws IOException {
    expectedEx.expect(IllegalStateException.class);
    expectedEx.expectMessage("Workbook has been closed.");
    readerNH.close();
    readerNH.getHeader();
  }

  @Test
  public void testGetCurrentSheetName() {
    assertEquals("PII_20130328154417", reader.getCurrentSheetName());
  }

  @Test
  public void testGetAllSheetNames() {
    assertEquals(ra("PII_20130328154417"), reader.getAllSheetNames());
  }

  @Test
  public void testGetSheetsException() throws IOException {
    expectedEx.expect(IllegalStateException.class);
    expectedEx.expectMessage("Workbook has been closed.");
    reader.close();
    reader.getAllSheetNames();
  }

  @Test
  public void testTurnToSheet() {
    assertEquals("PII_20130328154417", reader.turnToSheet(0)
        .getCurrentSheetName());
    assertEquals("PII_20130328154417", reader.turnToSheet("PII_20130328154417")
        .getCurrentSheetName());
    assertEquals(ra(), reader.turnToSheet(0, false).getHeader());
    assertEquals(ra(), reader.turnToSheet("PII_20130328154417", false)
        .getHeader());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testTurnToSheetException1() {
    reader.turnToSheet(1);
  }

  @Test
  public void testTurnToSheetException2() throws IOException {
    expectedEx.expect(IllegalStateException.class);
    expectedEx.expectMessage("Workbook has been closed.");
    reader.close();
    reader.turnToSheet(0);
  }

  @Test
  public void testTurnToSheetException3() throws IOException {
    expectedEx.expect(IllegalStateException.class);
    expectedEx.expectMessage("Workbook has been closed.");
    reader.close();
    reader.turnToSheet(0, false);
  }

  @Test
  public void testTurnToSheetException4() throws IOException {
    expectedEx.expect(IllegalArgumentException.class);
    expectedEx.expectMessage("Sheet name is not found.");
    reader.turnToSheet("hahaha");
  }

  @Test
  public void testTurnToSheetException5() throws IOException {
    expectedEx.expect(IllegalArgumentException.class);
    expectedEx.expectMessage("Sheet name is not found.");
    reader.turnToSheet("hahaha", true);
  }

  @Test
  public void testToCSV() {
    assertTrue(reader.toCSV() instanceof Iterable);
    assertEquals(9, ra(reader.toCSV()).count());
    assertEquals(firstLineCSV.join(","), ra(reader.toCSV()).first());
    assertTrue(readerNH.toCSV() instanceof Iterable);
    assertEquals(10, ra(readerNH.toCSV()).count());
    assertEquals(header.join(","), ra(readerNH.toCSV()).first());
  }

  @Test(expected = IllegalStateException.class)
  public void testToCSVException() throws IOException {
    reader.close();
    reader.toCSV();
  }

  @Test
  public void testToLists() {
    assertTrue(reader.toLists() instanceof Iterable);
    assertEquals(9, ra(reader.toLists()).count());
    assertEquals(firstLine, ra(reader.toLists()).first());
    assertTrue(readerNH.toLists() instanceof Iterable);
    assertEquals(10, ra(readerNH.toLists()).count());
    assertEquals(header, ra(readerNH.toLists()).first());
  }

  @Test
  public void testToListsException() throws IOException {
    expectedEx.expect(IllegalStateException.class);
    expectedEx.expectMessage("Workbook has been closed.");
    reader.close();
    reader.toLists();
  }

  @Test
  public void testToArrays() {
    assertTrue(reader.toArrays() instanceof Iterable);
    assertEquals(9, ra(reader.toArrays()).count());
    assertArrayEquals(firstLine.toArray(), ra(reader.toArrays()).first());
    assertTrue(readerNH.toArrays() instanceof Iterable);
    assertEquals(10, ra(readerNH.toArrays()).count());
    assertArrayEquals(header.toArray(), ra(readerNH.toArrays()).first());
  }

  @Test
  public void testToArraysException() throws IOException {
    expectedEx.expect(IllegalStateException.class);
    expectedEx.expectMessage("Workbook has been closed.");
    reader.close();
    reader.toArrays();
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testToMaps() {
    assertTrue(reader.toMaps() instanceof Iterable);
    assertEquals(9, ra(reader.toMaps()).count());
    assertEquals(Hash(header.zip(firstLine)), reader.toMaps().iterator().next());
  }

  @Test
  public void testToMapsException1() {
    expectedEx.expect(IllegalStateException.class);
    expectedEx.expectMessage("Header is not found.");
    reader.turnToSheet(0, false);
    reader.toMaps();
  }

  @Test
  public void testToMapsException2() throws IOException {
    expectedEx.expect(IllegalStateException.class);
    expectedEx.expectMessage("Workbook has been closed.");
    reader.close();
    reader.toMaps();
  }

}
