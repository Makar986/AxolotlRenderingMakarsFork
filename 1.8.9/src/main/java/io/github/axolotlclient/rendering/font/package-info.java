/*
 * Copyright © 2025 moehreag <moehreag@gmail.com> & Contributors
 *
 * This file is part of AxolotlClient (Rendering Mod).
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 * For more information, see the LICENSE file.
 */

///
/// This package contains a port of https://github.com/mattvchandler/textogl
/// to Java.
///
/// Original License: MIT
///
/// Notable changes:
/// - integration with Minecraft's rendering engine, therefore the buffer management is left out.
/// - Slightly optimized font atlas packing
/// - support for Minecraft's formatted text system (all formatting codes/styling options except for custom fonts obviously)
/// - support for variable fonts
///
/// Version-specific note: This package also contains some utilities to improve font handling with
/// this old game version, therefore the note above applies to the `Font` class.
///
package io.github.axolotlclient.rendering.font;

