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

package com.io7m.jwheatsheaf.tests;

import com.io7m.jaffirm.core.PreconditionViolationException;
import com.io7m.jwheatsheaf.api.JWFileChooserConfiguration;
import com.io7m.jwheatsheaf.api.JWFileChooserFilterType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.file.FileSystems;
import java.nio.file.Path;

public final class JWFileChooserConfigurationTest
{
  /**
   * Trying to specify a default filter that doesn't appear in the list of
   * filters fails.
   */

  @Test
  public void testFilterNotInList()
  {
    Assertions.assertThrows(PreconditionViolationException.class, () -> {
      JWFileChooserConfiguration.builder()
        .setFileSystem(FileSystems.getDefault())
        .setFileFilterDefault(new EmptyFilter())
        .build();
    });
  }

  private static final class EmptyFilter
    implements JWFileChooserFilterType
  {
    EmptyFilter()
    {

    }

    @Override
    public String description()
    {
      return "failure";
    }

    @Override
    public boolean isAllowed(
      final Path path)
    {
      return false;
    }
  }
}
