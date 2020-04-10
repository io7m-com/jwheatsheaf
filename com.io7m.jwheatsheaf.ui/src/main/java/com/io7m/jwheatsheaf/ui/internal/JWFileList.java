/*
 * Copyright © 2020 Mark Raynsford <code@io7m.com> http://io7m.com
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR
 * IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package com.io7m.jwheatsheaf.ui.internal;

import com.io7m.jwheatsheaf.api.JWFileChooserFilterType;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;

import java.util.List;
import java.util.Objects;

/**
 * A filtered file list.
 */

public final class JWFileList
{
  private final ObservableList<JWFileItem> items;
  private final FilteredList<JWFileItem> filtered;
  private volatile String search;
  private volatile JWFileChooserFilterType filter;

  /**
   * Construct a filtered file list.
   *
   * @param inFilter The initial filter
   */

  public JWFileList(
    final JWFileChooserFilterType inFilter)
  {
    this.filter =
      Objects.requireNonNull(inFilter, "inFilter");
    this.items =
      FXCollections.observableArrayList();
    this.search =
      "";
    this.filtered =
      this.items.filtered(this::isItemVisible);
  }

  private boolean isItemVisible(
    final JWFileItem item)
  {
    final boolean allowed =
      this.filter.isAllowed(item.path());
    final boolean searched =
      item.name().toUpperCase().contains(this.search.toUpperCase());
    return allowed && searched;
  }

  public void setItems(
    final List<JWFileItem> newItems)
  {
    this.items.setAll(newItems);
  }

  public void setSearch(
    final String searchText)
  {
    this.search = Objects.requireNonNull(searchText, "searchText");
    this.items.setAll(List.copyOf(this.items));
  }

  public void setFilter(
    final JWFileChooserFilterType newFilter)
  {
    this.filter = Objects.requireNonNull(newFilter, "filter");
    this.items.setAll(List.copyOf(this.items));
  }

  public ObservableList<JWFileItem> items()
  {
    return this.filtered;
  }
}
