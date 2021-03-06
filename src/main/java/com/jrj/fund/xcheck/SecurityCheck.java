/**
 * cdp4j Commercial License
 *
 * Copyright 2017, 2019 WebFolder OÜ
 *
 * Permission  is hereby  granted,  to "____" obtaining  a  copy of  this software  and
 * associated  documentation files  (the "Software"), to deal in  the Software  without
 * restriction, including without limitation  the rights  to use, copy, modify,  merge,
 * publish, distribute  and sublicense  of the Software,  and to permit persons to whom
 * the Software is furnished to do so, subject to the following conditions:
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR  IMPLIED,
 * INCLUDING  BUT NOT  LIMITED  TO THE  WARRANTIES  OF  MERCHANTABILITY, FITNESS  FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL  THE AUTHORS  OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE
 * OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.jrj.fund.xcheck;

import static io.webfolder.cdp.session.SessionFactory.DEFAULT_PORT;
import static java.lang.System.getProperty;
import static java.nio.file.Paths.get;
import static java.util.Arrays.asList;

import java.io.IOException;
import java.net.ServerSocket;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import io.webfolder.cdp.Launcher;
import io.webfolder.cdp.event.Events;
import io.webfolder.cdp.event.security.CertificateError;
import io.webfolder.cdp.event.security.SecurityStateChanged;
import io.webfolder.cdp.session.Session;
import io.webfolder.cdp.session.SessionFactory;

public class SecurityCheck {

	public static void main(String[] args) {

		Set<String> finished = new HashSet<>();
		new Thread() {

			public void run() {
				Launcher launcher = new Launcher(getFreePort(DEFAULT_PORT));
				Path remoteProfileData = get(getProperty("java.io.tmpdir"))
						.resolve("remote-profile-" + new Random().nextInt());
				SessionFactory factory = launcher.launch(asList(
//                		"--headless",
						"--disable-gpu", "--allow-running-insecure-content",
						"--user-data-dir=" + remoteProfileData.toString()));

				try (SessionFactory sf = factory) {
					try (Session session = sf.create()) {
						session.getCommand().getSecurity().enable();
						session.getCommand().getRuntime().enable();
						session.addEventListener((e, d) -> {
							if (Events.SecuritySecurityStateChanged.equals(e)) {
								System.out.println("SecuritySecurityStateChanged");
								SecurityStateChanged ss = (SecurityStateChanged) d;
								System.out.println(ss.getSecurityState());
								System.out.println(ss.getSummary());
//								System.out.println(ss.getExplanations().stream().map(a -> a.getDescription())
//										.collect(Collectors.joining("\n")));
							}
							if (Events.SecurityCertificateError.equals(e)) {
								System.out.println("SecurityCertificateError");
								CertificateError ss = (CertificateError) d;
								System.out.println(ss.getRequestURL());
								System.out.println(ss.getErrorType());
							}

						});
						session.navigate("https://glink.genius.com.cn/");
						session.waitDocumentReady();
						session.wait(3000);
					}
				}
			}

		}.start();
	}

	protected static int getFreePort(int portNumber) {
		try (ServerSocket socket = new ServerSocket(portNumber)) {
			int freePort = socket.getLocalPort();
			return freePort;
		} catch (IOException e) {
			return getFreePort(portNumber + 1);
		}
	}
}
