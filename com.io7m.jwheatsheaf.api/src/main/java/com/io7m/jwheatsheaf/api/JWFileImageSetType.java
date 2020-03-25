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

package com.io7m.jwheatsheaf.api;

import java.net.URL;
import java.util.Optional;

/**
 * An image resolver used to supply icon resources for the file chooser UI.
 */

public interface JWFileImageSetType
{
  /**
   * @return An icon for the "create directory" button
   */

  URL forDirectoryCreate();

  /**
   * @return An icon for the "parent directory" button
   */

  URL forDirectoryUp();

  /**
   * @return An icon for the "recent items" entry
   */

  URL forRecentItems();

  /**
   * @return An icon for filesystem roots
   */

  URL forFileSystem();

  /**
   * Find an icon for the given file kind.
   *
   * @param kind The file kind
   *
   * @return An icon for the given kind
   */

  Optional<URL> forFileKind(JWFileKind kind);
}
