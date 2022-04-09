/*
 * Copyright © 2020 Mark Raynsford <code@io7m.com> https://www.io7m.com
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

package com.io7m.jwheatsheaf.api;

/**
 * A specification of the action the file chooser is performing. This
 * affects the behaviour of the chooser.
 */

public enum JWFileChooserAction
{
  /**
   * The user is choosing a single existing file. This means that a
   * file (or directory) must be selected in the directory table for the "OK"
   * button to be enabled.
   */

  OPEN_EXISTING_SINGLE,

  /**
   * The user is choosing a set of existing files. This means that at least one
   * file (or directory) must be selected in the directory table for the "OK"
   * button to be enabled.
   */

  OPEN_EXISTING_MULTIPLE,

  /**
   * The user is choosing a single file that may be created by the application.
   * This means that a either name must be specified in the file name field,
   * or a file must be selected in the directory table for the for the "OK"
   * button to be enabled.
   */

  CREATE
}
