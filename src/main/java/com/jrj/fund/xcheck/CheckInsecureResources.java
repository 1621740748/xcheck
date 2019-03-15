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

import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.nio.file.Path;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import io.webfolder.cdp.Launcher;
import io.webfolder.cdp.event.Events;
import io.webfolder.cdp.event.network.RequestWillBeSent;
import io.webfolder.cdp.session.Session;
import io.webfolder.cdp.session.SessionFactory;
import io.webfolder.cdp.type.security.MixedContentType;

public class CheckInsecureResources {
	private static ExecutorService service = Executors.newFixedThreadPool(5);

	public void checkUrl(String url) {
		service.submit(new Thread() {
			public void run() {
				Launcher launcher = new Launcher(getFreePort(DEFAULT_PORT));
				Path remoteProfileData = get(getProperty("java.io.tmpdir"))
						.resolve("remote-profile-" + new Random().nextInt());
				SessionFactory factory = launcher.launch(asList(
						"--disable-gpu",
						"--allow-running-insecure-content",
						"--user-data-dir=" + remoteProfileData.toString()));

				try (SessionFactory sf = factory) {
					try (Session session = sf.create()) {
						session.getCommand().getNetwork().enable();
						session.addEventListener((e, d) -> {
							if (Events.NetworkRequestWillBeSent.equals(e)) {
								RequestWillBeSent s = (RequestWillBeSent) d;
								if (MixedContentType.Blockable.equals(s.getRequest().getMixedContentType())) {
									System.out.println(s.getRequest().getUrl());
									// System.out.println("--"+s.getDocumentURL());
									if (s.getInitiator() != null && s.getInitiator().getUrl() != null) {
										System.out.println("--" + s.getInitiator().getUrl());
									}
									if (s.getInitiator() != null && s.getInitiator().getStack() != null) {
										System.out.println("*****" + s.getInitiator().getStack().getCallFrames()
												.stream().map(a -> a.getUrl()).distinct()
												.collect(Collectors.joining("\n*****")));
									}
								}
							}
						});
						session.navigate(url);
						session.waitDocumentReady();
						session.wait(3000);
					}
				}
			}

		});
	}

	public static void main(String[] args) {
		CheckInsecureResources cir=new CheckInsecureResources();
		//String url="https://fund.jrj.com.cn";
		String file="/seeds.txt";
		try {
			List<String> urls=IOUtils.readLines(CheckInsecureResources.class.getResourceAsStream(file), "utf-8");
			if(urls!=null&&!urls.isEmpty()) {
				urls.stream()
				.filter(u->StringUtils.isNotBlank(u))
				.forEach(u->{cir.checkUrl(u);});
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	
		
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
