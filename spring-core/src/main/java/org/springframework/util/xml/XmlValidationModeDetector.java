/*
 * Copyright 2002-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.util.xml;

import java.io.BufferedReader;
import java.io.CharConversionException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

/**
 * XML 验证模式探测器。
 * 自动获取验证模式，DTO 获取 XSD
 *
 * Detects whether an XML stream is using DTD- or XSD-based validation.
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 2.0
 */
public class XmlValidationModeDetector {

	/**
	 * Indicates that the validation should be disabled.
	 */
	public static final int VALIDATION_NONE = 0;

	/**
	 * Indicates that the validation mode should be auto-guessed, since we cannot find
	 * a clear indication (probably choked on some special characters, or the like).
	 */
	public static final int VALIDATION_AUTO = 1;

	/**
	 * Indicates that DTD validation should be used (we found a "DOCTYPE" declaration).
	 */
	public static final int VALIDATION_DTD = 2;

	/**
	 * Indicates that XSD validation should be used (found no "DOCTYPE" declaration).
	 */
	public static final int VALIDATION_XSD = 3;


	/**
	 * The token in a XML document that declares the DTD to use for validation
	 * and thus that DTD validation is being used.
	 */
	private static final String DOCTYPE = "DOCTYPE";

	/**
	 * The token that indicates the start of an XML comment.
	 */
	private static final String START_COMMENT = "<!--";

	/**
	 * The token that indicates the end of an XML comment.
	 */
	private static final String END_COMMENT = "-->";


	/**
	 * Indicates whether or not the current parse position is inside an XML comment.
	 */
	private boolean inComment;


	/**
	 * Detect the validation mode for the XML document in the supplied {@link InputStream}.
	 * Note that the supplied {@link InputStream} is closed by this method before returning.
	 * @param inputStream the InputStream to parse
	 * @throws IOException in case of I/O failure
	 * @see #VALIDATION_DTD
	 * @see #VALIDATION_XSD
	 */
	public int detectValidationMode(InputStream inputStream) throws IOException {
		// Peek into the file to look for DOCTYPE.
		// 将 inputStream 封装成 BufferedReader
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		try {
			// // 是否为 DTD 校验模式。默认为 XSD 模式
			boolean isDtdValidated = false;
			String content;
			// 循环读取文件内容
			while ((content = reader.readLine()) != null) {
				// 清除一行上的所有注释内容，并返回剩余内容。
				content = consumeCommentTokens(content);
				// 如果该行内容是注释或者，content为空，跳过，读下一行
				if (this.inComment || !StringUtils.hasText(content)) {
					continue;
				}
				// 如果内容包含 DOCTYPE 则为 DTO 模式
				if (hasDoctype(content)) {
					isDtdValidated = true;
					break;
				}
				// 内容不包含 DOCTYPE
				// 并且 这一行包含 < ，并且 < 紧跟着的是字母，则为 XSD 验证模式
				if (hasOpeningTag(content)) {
					// End of meaningful data...
					break;
				}
			}
			// 返回相应的模式
			return (isDtdValidated ? VALIDATION_DTD : VALIDATION_XSD);
		}
		catch (CharConversionException ex) {
			// 如果发生异常，返回自动模式
			// Choked on some character encoding...
			// Leave the decision up to the caller.
			return VALIDATION_AUTO;
		}
		finally {
			reader.close();
		}
	}


	/**
	 * Does the content contain the DTD DOCTYPE declaration?
	 */
	private boolean hasDoctype(String content) {
		return content.contains(DOCTYPE);
	}

	/**
	 * Does the supplied content contain an XML opening tag. If the parse state is currently
	 * in an XML comment then this method always returns false. It is expected that all comment
	 * tokens will have consumed for the supplied content before passing the remainder to this method.
	 */
	private boolean hasOpeningTag(String content) {
		// 如果该行内容是注释，直接返回false
		if (this.inComment) {
			return false;
		}
		int openTagIndex = content.indexOf('<');
		// openTagIndex > -1：'<' 存在
		// content.length() > openTagIndex + 1 ： 表示 '<' 后面还有其他内容
		// Character.isLetter(content.charAt(openTagIndex + 1))：表示 '<' 后面的内容是字母
		// 上面上个条件全部满足，返回true，有一个不满足 返回false
		return (openTagIndex > -1 && (content.length() > openTagIndex + 1) &&
				Character.isLetter(content.charAt(openTagIndex + 1)));
	}

	/**
	 * 清除一行上的所有注释内容，并返回剩余内容。
	 *
	 *
	 * Consumes all the leading comment data in the given String and returns the remaining content, which
	 * may be empty since the supplied content might be all comment data. For our purposes it is only important
	 * to strip leading comment content on a line since the first piece of non comment content will be either
	 * the DOCTYPE declaration or the root element of the document.
	 */
	@Nullable
	private String consumeCommentTokens(String line) {
		// 如果该行不包含 '<!--'，并且也不包含 '-->'，直接返回。
		if (!line.contains(START_COMMENT) && !line.contains(END_COMMENT)) {
			return line;
		}
		String currLine = line;
		// 该行包含 '<!--'， 或者'-->'，执行 consume
		while ((currLine = consume(currLine)) != null) {
			// currLine返回的是'<!--'后的内容，或者'-->'后的内容
			// 如果内容不是注释，并且当前行不以'<!--'开头，直接返回内容
			if (!this.inComment && !currLine.trim().startsWith(START_COMMENT)) {
				return currLine;
			}
		}
		// 若该行内容是注释行，返回null
		return null;
	}

	/**
	 * Consume the next comment token, update the "inComment" flag
	 * and return the remaining content.
	 */
	@Nullable
	private String consume(String line) {
		// 一开始，inComment == false，执行startComment方法，inComment变成true，返回的index是'<!--'后一个内容的索引，最后返回的是'<!--'后的内容
		// 再次循环时，inComment == true, 执行endComment方法，
		// 		如果不存在'-->'，返回-1，最终返回null，则该行就会被忽略掉
		// 		如果存在'-->'，inComment变成false，返回的是'-->'后一个内容的索引值，最后返回的是'-->'后的内容，内容有可能是空的
		int index = (this.inComment ? endComment(line) : startComment(line));
		return (index == -1 ? null : line.substring(index));
	}

	/**
	 * Try to consume the {@link #START_COMMENT} token.
	 * @see #commentToken(String, String, boolean)
	 */
	private int startComment(String line) {
		// 如果存在 <!--'，返回的是'<!--'后一个内容的索引值，否则返回-1
		return commentToken(line, START_COMMENT, true);
	}

	private int endComment(String line) {
		// 如果存在'-->'，返回的是 '-->'后一个内容的索引值，否则返回-1
		return commentToken(line, END_COMMENT, false);
	}

	/**
	 * Try to consume the supplied token against the supplied content and update the
	 * in comment parse state to the supplied value. Returns the index into the content
	 * which is after the token or -1 if the token is not found.
	 */
	private int commentToken(String line, String token, boolean inCommentIfPresent) {
		// 获取 '<!--' 或者 '-->' 的索引值
		// 该索引值，是'<!--' 或者 '-->'第一个字符的位置
		int index = line.indexOf(token);
		// 索引值大于 -1 ，说明存在，设置 inComment
		if (index > - 1) {
			this.inComment = inCommentIfPresent;
		}
		// 如果索引值为-1，返回-1，否则，返回索引值 + token长度
		// 返回 -1 ：表示不存在注释
		// index + token.length()：
		// 		'<!--' : 返回的是'<!--'后一个内容的索引值
		//      '-->' : 返回的是 '-->'后一个内容的索引值
		return (index == -1 ? index : index + token.length());
	}

}
