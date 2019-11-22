/*
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

import static net.sf.rubycollect4j.RubyCollections.ra;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.google.common.base.MoreObjects;
import com.google.common.testing.EqualsTester;
import com.google.common.testing.NullPointerTester;

import net.sf.rubycollect4j.RubyFile;

public class WorkbookWriterTest {

  private static final String BASE_DIR = "src/test/resources";
  private WorkbookWriter writer;

  @Rule
  public ExpectedException expectedEx = ExpectedException.none();

  @Before
  public void setUp() throws Exception {
    writer = new WorkbookWriter();
  }

  // @Test
  // public void testNullProof() {
  // expectedEx.expect(NullPointerException.class);
  // expectedEx.expectMessage("Parameter<Workbook> is not nullable");
  // WorkbookWriter.open(null);
  // }

  @Test
  public void testAllConstructorsNPE() {
    new NullPointerTester().testAllPublicConstructors(WorkbookWriter.class);
  }

  @Test
  public void testAllPublicMethodsNPE() throws Exception {
    new NullPointerTester()
        .ignore(WorkbookWriter.class.getDeclaredMethod("equals", Object.class))
        .testAllPublicInstanceMethods(writer);
  }

  @Test
  public void testAllPublicStaticMethodsNPE() {
    new NullPointerTester().testAllPublicStaticMethods(WorkbookWriter.class);
  }

  @Test
  public void testConstructor() {
    assertTrue(writer instanceof WorkbookWriter);
    assertTrue(
        new WorkbookWriter(new HSSFWorkbook()) instanceof WorkbookWriter);
    Workbook wb = new HSSFWorkbook();
    wb.createSheet();
    assertTrue(new WorkbookWriter(wb) instanceof WorkbookWriter);
    assertTrue(WorkbookWriter.open(wb) instanceof WorkbookWriter);
    assertTrue(WorkbookWriter.openXLSX() instanceof WorkbookWriter);
    assertTrue(WorkbookWriter.openXLS() instanceof WorkbookWriter);
  }

  @Test
  public void testWorkbookFormat() {
    assertTrue(WorkbookWriter.openXLSX().getWorkbook() instanceof XSSFWorkbook);
    assertTrue(WorkbookWriter.openXLS().getWorkbook() instanceof HSSFWorkbook);
  }

  @Test
  public void testSetSheetName() {
    assertEquals(ra("Sheet0"), writer.getAllSheetNames());
    assertEquals(ra("NewSheet"),
        writer.setSheetName("NewSheet").getAllSheetNames());
  }

  @Test
  public void testGetWorkbook() {
    assertTrue(writer.getWorkbook() instanceof Workbook);
  }

  @Test
  public void testGetCurrentSheetName() {
    assertEquals("Sheet0", writer.getCurrentSheetName());
  }

  @Test
  public void testGetAllSheetNames() {
    assertEquals(ra("Sheet0"), writer.getAllSheetNames());
  }

  @Test
  public void testCreateSheet() {
    writer.createSheet("test");
    assertTrue(writer.getAllSheetNames().contains("test"));
    assertEquals("Sheet0", writer.getCurrentSheetName());
  }

  @Test
  public void testCreateSheetException() {
    expectedEx.expect(IllegalArgumentException.class);
    expectedEx.expectMessage("Sheet name is already existed");
    writer.createSheet("test");
    writer.createSheet("test");
  }

  @Test
  public void testTurnToSheet() {
    writer.createSheet("test");
    writer.turnToSheet("test");
    assertEquals("test", writer.getCurrentSheetName());
    writer.turnToSheet(0);
    assertEquals("Sheet0", writer.getCurrentSheetName());
  }

  @Test
  public void testTurnToSheetException1() {
    expectedEx.expect(IllegalArgumentException.class);
    expectedEx.expectMessage("Sheet name is not found");
    writer.turnToSheet("hahaha");
  }

  @Test(expected = IndexOutOfBoundsException.class)
  public void testTurnToSheetException2() {
    writer.turnToSheet(99);
  }

  @Test
  public void testCreateAndTurnToSheet() {
    writer.createAndTurnToSheet("test");
    assertEquals("test", writer.getCurrentSheetName());
  }

  @Test
  public void testCreateAndTurnToSheetException() {
    expectedEx.expect(IllegalArgumentException.class);
    expectedEx.expectMessage("Sheet name is already existed");
    writer.createAndTurnToSheet("test");
    writer.createAndTurnToSheet("test");
  }

  @SuppressWarnings("resource")
  @Test
  public void testAddRow() {
    Calendar cal = Calendar.getInstance();
    Date date = new Date();
    writer.addRow("def");
    writer
        .addRow(null, true, cal, date, 1.1, new HSSFRichTextString("Hello!"),
            new XSSFRichTextString("World."), new HSSFWorkbook()
                .getCreationHelper().createHyperlink(HyperlinkType.URL),
            123, "abc");
    assertEquals("def", writer.getWorkbook().getSheetAt(0).rowIterator().next()
        .cellIterator().next().getStringCellValue());
    WorkbookWriter.openXLSX().addRow(new HSSFRichTextString("Hello!"),
        new XSSFRichTextString("World."));
    WorkbookWriter.openXLS().addRow(new HSSFRichTextString("Hello!"),
        new XSSFRichTextString("World."));

  }

  @Test
  public void testAddRowWithDouble() {
    writer.addRow(2.14540);
    assertEquals("2.1454",
        writer.toReader().withoutHeader().toLists().iterator().next().get(0));
  }

  @Test
  public void testSave() throws InvalidFormatException, IOException {
    writer.addRow("abc", "def");
    writer.save(RubyFile.join(BASE_DIR, "test.xls"));
    WorkbookReader reader = WorkbookReader
        .open(RubyFile.join(BASE_DIR, "test.xls")).withoutHeader();
    assertEquals("abc,def", reader.toCSV().iterator().next());
    reader.close();
    RubyFile.delete(RubyFile.join(BASE_DIR, "test.xls"));
  }

  @Test
  public void testToReader() {
    assertTrue(writer.toReader() instanceof WorkbookReader);
  }

  @Test
  public void testToBytes() throws IOException {
    Workbook wb = writer.getWorkbook();
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    try {
      wb.write(bos);
    } finally {
      bos.close();
    }
    assertArrayEquals(bos.toByteArray(), writer.toBytes());
  }

  @Test
  public void testEquality() {
    new EqualsTester()
        .addEqualityGroup(writer, new WorkbookWriter(), new WorkbookWriter())
        .testEquals();
  }

  @Test
  public void testUnequality() {
    assertNotEquals(writer, new WorkbookWriter().addRow("123"));
    assertNotEquals(writer.hashCode(),
        new WorkbookWriter().addRow("123").hashCode());
  }

  @Test
  public void testToString() {
    assertEquals(
        MoreObjects.toStringHelper(WorkbookWriter.class)
            .addValue(writer.toReader().toMultimap()).toString(),
        writer.toString());
  }

}
