/*
 * Copyright 2018-2019 Leon Linhart
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * A minmal Java library which provides a convenient way to parse command line parameters (for Java 8 and later).
 *
 * <p>MJL Options comes with a custom specification for command line parameters that is meant to provide a sane and
 * predictable format.</p>
 *
 * @see com.github.themrmilchmann.mjl.options.OptionParser
 *
 * @since   0.1.0
 */
module com.github.gw2toolbelt.gw2ml {

    requires static jsr305;

    exports com.github.gw2toolbelt.gw2ml;

}