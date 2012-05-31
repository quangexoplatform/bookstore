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
package org.exoplatform.test.rest;

import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.test.BookStoreService;
import org.exoplatform.test.entity.Book;
import org.exoplatform.test.entity.Book.CATEGORY;
import org.exoplatform.test.exception.BookNotFoundException;
import org.exoplatform.test.exception.DuplicateBookException;
import org.exoplatform.test.utils.Utils;

/**
 * Created by The eXo Platform SAS
 * Author : phong tran
 *          phongth@exoplatform.com
 * Jun 14, 2011  
 */
@Path("/bookstore")
public class BookStoreRestService implements ResourceContainer {
  private static final CacheControl cc;
  static {
    cc = new CacheControl();
    cc.setNoCache(true);
    cc.setNoStore(true);
  }
  
  private BookStoreService bookStoreService;
  
  public BookStoreRestService(BookStoreService bookStoreService) {
    this.bookStoreService = bookStoreService;
  }

  @GET
  @Path("/get/{bookId}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getBook(@PathParam("bookId") String bookId) throws DuplicateBookException {
    Book book = bookStoreService.getBook(bookId);

    if (book == null) {
      return Response.status(Status.NO_CONTENT).build();
    }
    return Response.ok(book, MediaType.APPLICATION_JSON).cacheControl(cc).build();
  }

  @POST
  @Path("/add")
  public Response addBook(@FormParam("bookName") String bookName,
                          @FormParam("category") String category,
                          @FormParam("content") String content) {
    CATEGORY categoryEnum = null;
    try {
      categoryEnum = Utils.bookCategoryStringToEnum(category);
    } catch (IllegalArgumentException ex) {
      return Response.status(Status.BAD_REQUEST).build();
    }

    try {
      bookStoreService.addBook(bookName, categoryEnum, content);
    } catch (DuplicateBookException e) {
      return Response.status(Status.CONFLICT).build();
    }
    return Response.ok().cacheControl(cc).build();
  }

  @GET
  @Path("/size")
  public Response getNumberOfBooks() {
    return Response.ok(String.valueOf(bookStoreService.getAllBook().size()), MediaType.APPLICATION_JSON)
                   .cacheControl(cc)
                   .build();
  }

  @DELETE
  @Path("/delete/{bookId}")
  public Response deleteBook(@PathParam("bookId") String bookId) {
    try {
      bookStoreService.deleteBook(bookId);
    } catch (BookNotFoundException e) {
      return Response.status(Status.NOT_FOUND).build();
    }

    return Response.ok().cacheControl(cc).build();
  }

  @DELETE
  @Path("/delete")
  public Response deleteAll() {
    bookStoreService.deleteAll();
    return Response.ok().cacheControl(cc).build();
  }

  @POST
  @Path("/update")
  public Response editBook(@FormParam("bookId") String bookId,
                           @FormParam("bookName") String bookName,
                           @FormParam("category") String category,
                           @FormParam("content") String content) {
    CATEGORY categoryEnum = null;
    try {
      categoryEnum = Utils.bookCategoryStringToEnum(category);
    } catch (IllegalArgumentException ex) {
      return Response.status(Status.BAD_REQUEST).build();
    }
    
    Book book = new Book(bookName, categoryEnum, content);
    book.setId(bookId);

    try {
      bookStoreService.editBook(book);
    } catch (BookNotFoundException e) {
      return Response.status(Status.NOT_FOUND).build();
    }

    return Response.ok().cacheControl(cc).build();
  }

  @GET
  @Path("/search/{keyword}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response searchName(@PathParam("keyword") String key) {
    List<Book> books = bookStoreService.searchName(key);
    return Response.ok(books, MediaType.APPLICATION_JSON).cacheControl(cc).build();
  }
}
