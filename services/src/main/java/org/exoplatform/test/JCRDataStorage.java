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

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.util.IdGenerator;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.test.entity.Book;
import org.exoplatform.test.exception.BookNotFoundException;
import org.exoplatform.test.exception.DuplicateBookException;
import org.exoplatform.test.utils.PropertyReader;
import org.exoplatform.test.utils.Utils;

/**
 * Created by The eXo Platform SAS
 * Author : phong tran
 *          phongth@exoplatform.com
 * Jun 20, 2011  
 */
public class JCRDataStorage {
  private static final Log log = ExoLogger.getLogger(JCRDataStorage.class);
  public static final String DEFAULT_PARENT_PATH = "/bookStore"; 
  
  private RepositoryService repoService;
  
  public JCRDataStorage() {
  }
  
  void setRepositoryService(RepositoryService repoService) {
    this.repoService = repoService;
  }
  
  public void init() {
    SessionProvider sProvider = SessionProvider.createSystemProvider();
    Node node = null;
    try {
      node = getNodeByPath(DEFAULT_PARENT_PATH, sProvider);
    } catch (PathNotFoundException e) {
      // If the path not exist then create new path
      try {
        node = getNodeByPath("/", sProvider);
        node.addNode("bookStore", BookNodeTypes.EXO_BOOK_STORE);
        node.getSession().save();
      } catch (Exception e1) {
        e1.printStackTrace();
      }
    } catch (Exception e) {
      log.error("Failed to init BookStore jcr node's path", e);
    }  finally {
      sProvider.close();
    }
  }
  
  private Node getNodeByPath(String nodePath, SessionProvider sessionProvider) throws Exception {
    return (Node) getSession(sessionProvider).getItem(nodePath);
  }
  
  private Session getSession(SessionProvider sprovider) throws Exception {
    ManageableRepository currentRepo = repoService.getCurrentRepository();
    return sprovider.getSession(currentRepo.getConfiguration().getDefaultWorkspaceName(), currentRepo);
  }

  private Book createBookByNode(Node bookNode) throws Exception {
    if (bookNode == null) {
      return null;
    }
      
    Book bookNew = new Book();
    bookNew.setId(bookNode.getName());
    
    PropertyReader reader = new PropertyReader(bookNode);
    bookNew.setName(reader.string(BookNodeTypes.EXP_BOOK_NAME));
    bookNew.setCategory(Utils.bookCategoryStringToEnum(reader.string(BookNodeTypes.EXP_BOOK_CATEGORY)));
    bookNew.setContent(reader.string(BookNodeTypes.EXP_BOOK_CONTENT));
    return bookNew;
  }
  
  public Book getBook(String id) {
    SessionProvider sProvider = SessionProvider.createSystemProvider();
    try {
      Node node = getNodeByPath(DEFAULT_PARENT_PATH + "/" + id, sProvider);
      return createBookByNode(node);
    } catch (PathNotFoundException e) {
      return null;
    } catch (Exception e) {
      log.error("Failed to get book by id", e);
      return null;
    } finally {
      sProvider.close();
    }
  }
  
  public Book addBook(Book book) throws DuplicateBookException {
    SessionProvider sProvider = SessionProvider.createSystemProvider();
    if (isExistBookName(book.getName(), sProvider)) {
      throw new DuplicateBookException(String.format("Book %s is existed", book.getName()));
    }
    
    String nodeId = IdGenerator.generate();
    book.setId(nodeId);
    
    try {
      Node parentNode = getNodeByPath(DEFAULT_PARENT_PATH, sProvider);
      Node bookNode = parentNode.addNode(nodeId, BookNodeTypes.EXO_BOOK);
      bookNode.setProperty(BookNodeTypes.EXP_BOOK_NAME, book.getName());
      bookNode.setProperty(BookNodeTypes.EXP_BOOK_CATEGORY, Utils.bookCategoryEnumToString(book.getCategory()));
      bookNode.setProperty(BookNodeTypes.EXP_BOOK_CONTENT, book.getContent());
      
      parentNode.getSession().save();
      return book;
    } catch (PathNotFoundException e) {
      return null;
    } catch (Exception e) {
      log.error("Failed to add book", e);
      return null;
    } finally {
      sProvider.close();
    }
  }
  
  public void deleteBook(String id) throws BookNotFoundException {
    SessionProvider sProvider = SessionProvider.createSystemProvider();
    try {
      Node node = getNodeByPath(DEFAULT_PARENT_PATH + "/" + id, sProvider);
      node.remove();
      node.getSession().save();
    } catch (PathNotFoundException e) {
      throw new BookNotFoundException(String.format("Book %s is not found", id));
    } catch (Exception e) {
      log.error("Failed to delete book by id", e);
    } finally {
      sProvider.close();
    }
  }
  
  public void deleteAll() {
    SessionProvider sProvider = SessionProvider.createSystemProvider();
    try {
      Node parentNode = getNodeByPath(DEFAULT_PARENT_PATH, sProvider);
      parentNode.remove();
      parentNode.getSession().save();
      
      // Recreate parent path
      init();
    } catch (PathNotFoundException e) {
      log.error("Failed to delete all book", e);
    } catch (Exception e) {
      log.error("Failed to delete all book", e);
    } finally {
      sProvider.close();
    }
  }
  
  public Book editBook(Book book) throws BookNotFoundException {
    SessionProvider sProvider = SessionProvider.createSystemProvider();
    try {
      Node node = getNodeByPath(DEFAULT_PARENT_PATH + "/" + book.getId(), sProvider);
      node.setProperty(BookNodeTypes.EXP_BOOK_NAME, book.getName());
      node.setProperty(BookNodeTypes.EXP_BOOK_CATEGORY, Utils.bookCategoryEnumToString(book.getCategory()));
      node.setProperty(BookNodeTypes.EXP_BOOK_CONTENT, book.getContent());
      node.getSession().save();
    } catch (PathNotFoundException e) {
      throw new BookNotFoundException(String.format("Book %s is not found", book.getId()));
    } catch (Exception e) {
      log.error("Failed to delete all book", e);
    } finally {
      sProvider.close();
    }
    return null;
  }
  
  public boolean isExistBookName(String bookName) {
    SessionProvider sProvider = SessionProvider.createSystemProvider();
    try {
      return isExistBookName(bookName, sProvider);
    } finally {
      sProvider.close();
    }
  }
  
  private boolean isExistBookName(String bookName, SessionProvider sProvider) {
    bookName.replaceAll("\"", "\\\"").replaceAll("-", StringUtils.EMPTY);
    
    StringBuffer queryString = new StringBuffer("select * from " + BookNodeTypes.EXO_BOOK);
    queryString.append(" where " + BookNodeTypes.EXP_BOOK_NAME + " = '" + bookName + "'");
    
    try {
      QueryManager queryManager = getSession(sProvider).getWorkspace().getQueryManager();
      Query query = queryManager.createQuery(queryString.toString(), Query.SQL);
      QueryResult result = query.execute();
      NodeIterator iterator = result.getNodes();
      return iterator.hasNext();
    } catch (Exception e) {
      log.error("Failed to check exist book name", e);
      return false;
    }
  }
  
  public List<Book> getAllBook() {
    StringBuffer queryString = new StringBuffer("select * from " + BookNodeTypes.EXO_BOOK);
    SessionProvider sProvider = SessionProvider.createSystemProvider();
    try {
      QueryManager queryManager = getSession(sProvider).getWorkspace().getQueryManager();
      Query query = queryManager.createQuery(queryString.toString(), Query.SQL);
      QueryResult result = query.execute();
      NodeIterator iterator = result.getNodes();
      
      List<Book> books = new ArrayList<Book>();
      while (iterator.hasNext()) {
        Node node = iterator.nextNode();
        Book book = createBookByNode(node);
        books.add(book);
      }
      return books;
    } catch (Exception e) {
      log.error("Failed to get all book", e);
      return null;
    } finally {
      sProvider.close();
    }
  }
  
  public List<Book> searchName(String key) {
    key = key.replaceAll("\"", "\\\"").replaceAll("-", StringUtils.EMPTY);
    
    StringBuffer queryString = new StringBuffer("select * from " + BookNodeTypes.EXO_BOOK);
    queryString.append(" where " + BookNodeTypes.EXP_BOOK_NAME + " like '%" + key + "%'");
    
    SessionProvider sProvider = SessionProvider.createSystemProvider();
    try {
      QueryManager queryManager = getSession(sProvider).getWorkspace().getQueryManager();
      Query query = queryManager.createQuery(queryString.toString(), Query.SQL);
      QueryResult result = query.execute();
      NodeIterator iterator = result.getNodes();
      
      List<Book> books = new ArrayList<Book>();
      while (iterator.hasNext()) {
        Node node = iterator.nextNode();
        Book book = createBookByNode(node);
        books.add(book);
      }
      return books;
    } catch (Exception e) {
      log.error("Failed to search book by name", e);
      return null;
    } finally {
      sProvider.close();
    }
  }
}
