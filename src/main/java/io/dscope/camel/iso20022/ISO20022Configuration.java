/*
 * Copyright 2023 Exilor Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package io.dscope.camel.iso20022;

public class ISO20022Configuration implements Cloneable{
	public static final String XML_TYPE = "xml";
	public static final String JSON_TYPE = "json";
	public static final String DOM_TYPE = "dom";
	public static final String UNMARSHAL_PREFIX = "unmarshal";
	public static final String MARSHAL_PREFIX = "marshal";
}
