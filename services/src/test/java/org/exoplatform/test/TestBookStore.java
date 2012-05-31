/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.test;

import java.util.List;

import junit.framework.TestCase;

import org.exoplatform.container.StandaloneContainer;
import org.exoplatform.test.entity.Book;
import org.exoplatform.test.entity.Book.CATEGORY;
import org.exoplatform.test.exception.BookNotFoundException;
import org.exoplatform.test.exception.DuplicateBookException;

/**
 * Created by The eXo Platform SAS
 * Author : Lai Trung Hieu
 *          hieu.lai@exoplatform.com
 * 7 Jun 2011  
 */
public class TestBookStore extends TestCase {
  protected static BookStoreService service;

  protected static StandaloneContainer container;

  static {
    initContainer();
  }

  private static void initContainer() {
    try {
      String jcrConf = TestBookStore.class.getResource("/conf/portal/test-configuration.xml").toString();
      StandaloneContainer.addConfigurationURL(jcrConf);
      
      container = StandaloneContainer.getInstance();
      service = (BookStoreService) container.getComponentInstanceOfType(BookStoreService.class);
    } catch (Exception e) {
      throw new RuntimeException("Failed to initialize standalone container: " + e.getMessage(), e);
    }
  }
  
  @Override
  protected void setUp() throws Exception {
    service.deleteAll();
  }

  public void testAddAndGetBook() throws DuplicateBookException {
    String name = "Doraemon";
    CATEGORY category = CATEGORY.MANGA;
    String content = "Nobita & Doreamon";
    
    Book book = service.addBook(name, category, content);
    Book addedBook = service.getBook(book.getId());
    
    assertNotNull(addedBook);
    assertEquals(name, addedBook.getName());
    assertEquals(category, addedBook.getCategory());
    assertEquals(content, addedBook.getContent());
  }

  public void testEditBook() throws DuplicateBookException, BookNotFoundException {
    String oldName = "Gone with the wind";
    CATEGORY oldCategory = CATEGORY.NOVEL;
    String oldContent = "Content xyz";
    
    String newName = "Gone with the gold";
    CATEGORY newCategory = CATEGORY.TECHNICAL;
    String newContent = "Content abc";
    
    Book addedBook = service.addBook(oldName, oldCategory, oldContent);
    assertNotNull(addedBook);
    assertEquals(oldName, addedBook.getName());
    assertEquals(oldCategory, addedBook.getCategory());
    assertEquals(oldContent, addedBook.getContent());
    
    addedBook.setName("Gone with the gold");
    addedBook.setCategory(CATEGORY.TECHNICAL);
    addedBook.setContent(newContent);

    service.editBook(addedBook);
    Book editedBook = service.getBook(addedBook.getId());
    
    assertNotNull(editedBook);
    assertEquals(newName, editedBook.getName());
    assertEquals(newCategory, editedBook.getCategory());
    assertEquals(newContent, editedBook.getContent());
  }

  public void testDeleteBook() throws DuplicateBookException, BookNotFoundException {
    String name = "Doraemon";
    CATEGORY category = CATEGORY.MANGA;
    String content = "Nobita & Doreamon";
    
    Book addedBook = service.addBook(name, category, content);
    assertNotNull(addedBook);
    
    service.deleteBook(addedBook.getId());
    Book deletedBook = service.getBook(addedBook.getId());
    assertNull(deletedBook);
  }

  public void testGetAllBooks() throws DuplicateBookException {
    service.addBook("Doraemon1", CATEGORY.MANGA, "Nobita & Doreamon");
    service.addBook("Doraemon2", CATEGORY.MANGA, "Nobita & Doreamon");
    service.addBook("Doraemon3", CATEGORY.MANGA, "Nobita & Doreamon");
    service.addBook("Doraemon4", CATEGORY.MANGA, "Nobita & Doreamon");
    
    List<Book> books = service.getAllBook();
    assertTrue(books.size() == 4);
  }

  public void testDeleteAllBook() throws DuplicateBookException {
    service.addBook("Doraemon1", CATEGORY.MANGA, "Nobita & Doreamon");
    service.addBook("Doraemon2", CATEGORY.MANGA, "Nobita & Doreamon");
    
    List<Book> books1 = service.getAllBook();
    assertTrue(books1.size() == 2);
    
    service.deleteAll();
    
    List<Book> books2 = service.getAllBook();
    assertTrue(books2.size() == 0);
  }

  public void testSeachBook() throws DuplicateBookException {
    service.addBook("Doraemon1", CATEGORY.MANGA, "Nobita & Doreamon");
    service.addBook("Doraemon2", CATEGORY.MANGA, "Nobita & Doreamon");
    service.addBook("abc", CATEGORY.MANGA, "Nobita & Doreamon");
    
    List<Book> books = service.getAllBook();
    assertTrue(books.size() == 3);
    
    List<Book> searchResult = service.searchName("Doraemon");
    assertTrue(searchResult.size() == 2);
  }
  
  public void testIsExistBookName() throws DuplicateBookException {
    String bookName = "Doraemon1";
    
    assertFalse(service.isExistBookName(bookName));
    
    service.addBook(bookName, CATEGORY.MANGA, "Nobita & Doreamon");
    
    assertTrue(service.isExistBookName(bookName));
  }
}
