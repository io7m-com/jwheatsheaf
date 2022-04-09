/*
 * Copyright © 2021 Mark Raynsford <code@io7m.com> https://www.io7m.com
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

import java.util.Optional;

/**
 * The set of string overrides. For any of the methods in this class, if
 * {@link Optional#empty()} is returned, a default string will be used.
 *
 * @since 3.0.0
 */

public interface JWFileChooserStringOverridesType
{
  /**
   * A message override for the "Open" button.
   *
   * @return A message override
   */

  Optional<String> buttonOpen();

  /**
   * A message override for the "Save" button.
   *
   * @return A message override
   */

  Optional<String> buttonSave();

  /**
   * A message override for the "Do you want to replace this file?" message.
   *
   * @param file The file
   *
   * @return A message override
   */

  Optional<String> confirmReplaceMessage(String file);

  /**
   * A message override for the "Replace" confirmation button.
   *
   * @return A message override
   */

  Optional<String> confirmReplaceButton();

  /**
   * A message override for the "Replace?" confirmation dialog header.
   *
   * @return A message override
   */

  Optional<String> confirmTitleMessage();
}
