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
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import com.jrj.fund.xcheck.bo.BlockedResources;
import com.jrj.fund.xcheck.dao.BlockedResourcesDao;
import com.jrj.fund.xcheck.utils.DBUtils;
import com.jrj.fund.xcheck.utils.UrlUtils;

import io.webfolder.cdp.Launcher;
import io.webfolder.cdp.event.Events;
import io.webfolder.cdp.event.network.RequestWillBeSent;
import io.webfolder.cdp.session.Session;
import io.webfolder.cdp.session.SessionFactory;
import io.webfolder.cdp.type.security.MixedContentType;

public class CheckInsecureResourcesad {
	private static ExecutorService service = Executors.newFixedThreadPool(5);
	private static Semaphore smp = new Semaphore(1);

	public void checkUrl(String url) {
		service.submit(new Thread() {
			public void run() {
				try {
					smp.acquire();
					Launcher launcher = new Launcher(getFreePort(DEFAULT_PORT));
					Path remoteProfileData = get(getProperty("java.io.tmpdir"))
							.resolve("remote-profile-" + new Random().nextInt());
					//System.out.println(remoteProfileData);
					SessionFactory factory = launcher.launch(asList("--disable-gpu",
							"--ignore-certificate-errors",
							"--allow-running-insecure-content",
							"--user-data-dir=" + remoteProfileData.toString()));
                    System.out.println("---"+url);
					try (SessionFactory sf = factory) {
						try (Session session = sf.create()) {
							session.getCommand().getNetwork().enable();
							session.addEventListener((e, d) -> {
								if (Events.NetworkRequestWillBeSent.equals(e)) {
									RequestWillBeSent s = (RequestWillBeSent) d;
									if (MixedContentType.Blockable.equals(s.getRequest().getMixedContentType())) {
										BlockedResources br = new BlockedResources();
										br.setResUrl(s.getRequest().getUrl());
										br.setResHost(UrlUtils.getHost(s.getRequest().getUrl()));
										br.setResHostPath(UrlUtils.getHostAndPath(s.getRequest().getUrl()));
										System.out.println(br.getResUrl());
										if (StringUtils.isNotBlank(s.getDocumentURL())) {
											br.setPageUrl(s.getDocumentURL());
										} else {
											br.setPageUrl("");
										}
										if (s.getInitiator() != null && s.getInitiator().getUrl() != null) {
											br.setInitiateUrl(s.getInitiator().getUrl());
										} else {
											if (s.getInitiator() != null && s.getInitiator().getStack() != null) {
												br.setInitiateUrl(s.getInitiator().getStack().getCallFrames().stream()
														.map(a -> a.getUrl()).distinct()
														.collect(Collectors.joining("\n")));
											}
										}
										BlockedResourcesDao brDao = DBUtils.getInstance()
												.create(BlockedResourcesDao.class);
										brDao.add(br);
									}
								}
							});
							session.navigate(url);
							session.waitDocumentReady();
							session.wait(3000);
						}
					}

				} catch (Exception e) {

				}finally {
					smp.release();
				}
			}

		});
	}

	public static void main(String[] args) {
		CheckInsecureResourcesad cir = new CheckInsecureResourcesad();
		// String url="https://fund.jrj.com.cn";
		String file = "/seeds_ad.txt";
		try {
			List<String> urls = IOUtils.readLines(CheckInsecureResourcesad.class.getResourceAsStream(file), "utf-8");
			if (urls != null && !urls.isEmpty()) {
				urls.stream().filter(u -> StringUtils.isNotBlank(u)).map(u -> u.trim()).forEach(u -> {
					cir.checkUrl(u);
				});
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	protected synchronized static int getFreePort(int portNumber) {
		try (ServerSocket socket = new ServerSocket(portNumber)) {
			int freePort = socket.getLocalPort();
			return freePort;
		} catch (IOException e) {
			return getFreePort(portNumber + 1);
		}
	}
}
