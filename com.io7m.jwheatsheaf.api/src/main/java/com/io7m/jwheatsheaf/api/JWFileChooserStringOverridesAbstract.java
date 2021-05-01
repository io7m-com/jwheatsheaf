/*
 * Copyright © 2021 Mark Raynsford <code@io7m.com> http://io7m.com
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
 * An abstract set of string overrides that override nothing.
 */

public abstract class JWFileChooserStringOverridesAbstract
  implements JWFileChooserStringOverridesType
{
  protected JWFileChooserStringOverridesAbstract()
  {

  }

  /**
   * {@inheritDoc}
   *
   * Overrides of this method are NOT required to call this method via {@code super}.
   */

  @Override
  public Optional<String> buttonOpen()
  {
    return Optional.empty();
  }

  /**
   * {@inheritDoc}
   *
   * Overrides of this method are NOT required to call this method via {@code super}.
   */

  @Override
  public Optional<String> buttonSave()
  {
    return Optional.empty();
  }

  /**
   * {@inheritDoc}
   *
   * Overrides of this method are NOT required to call this method via {@code super}.
   */

  @Override
  public Optional<String> confirmReplaceMessage(
    final String file)
  {
    return Optional.empty();
  }

  /**
   * {@inheritDoc}
   *
   * Overrides of this method are NOT required to call this method via {@code super}.
   */

  @Override
  public Optional<String> confirmReplaceButton()
  {
    return Optional.empty();
  }

  /**
   * {@inheritDoc}
   *
   * Overrides of this method are NOT required to call this method via {@code super}.
   */

  @Override
  public Optional<String> confirmTitleMessage()
  {
    return Optional.empty();
  }
}
