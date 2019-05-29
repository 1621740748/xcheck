package com.jrj.fund.xcheck;

import static io.webfolder.cdp.event.Events.NetworkLoadingFinished;
import static io.webfolder.cdp.event.Events.NetworkResponseReceived;

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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import io.webfolder.cdp.Launcher;
import io.webfolder.cdp.event.network.LoadingFinished;
import io.webfolder.cdp.event.network.ResponseReceived;
import io.webfolder.cdp.session.Session;
import io.webfolder.cdp.session.SessionFactory;
import io.webfolder.cdp.type.network.GetResponseBodyResult;
import io.webfolder.cdp.type.network.ResourceType;
import io.webfolder.cdp.type.network.Response;

public class DownloadPage {
	private static ExecutorService service = Executors.newFixedThreadPool(5);
	private static Semaphore smp = new Semaphore(1);
	Set<String> finished = new HashSet<>();
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
							//	"--ignore-certificate-errors",
							"--allow-running-insecure-content",
							"--user-data-dir=" + remoteProfileData.toString()));
					System.out.println("---"+url);
					try (SessionFactory sf = factory) {
						try (Session session = sf.create()) {
							session.getCommand().getNetwork().enable();
							session.addEventListener((e, d) -> {
								if (NetworkLoadingFinished.equals(e)) {
									LoadingFinished lf = (LoadingFinished) d;
									finished.add(lf.getRequestId());
								}
								if (NetworkResponseReceived.equals(e)) {
									ResponseReceived rr = (ResponseReceived) d;
									Response response = rr.getResponse();
									if(response.getUrl().contains("bdimg.share.baidu.com")) {
										System.out.println("URL       : " + response.getUrl());
										if (finished.contains(rr.getRequestId()) && ResourceType.Document.equals(rr.getType())) {
											GetResponseBodyResult rb = session.getCommand().getNetwork().getResponseBody(rr.getRequestId());
											if (rb != null) {
												String body = rb.getBody();
											}
										}
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
		DownloadPage cir = new DownloadPage();
		// String url="https://fund.jrj.com.cn";
		try {
			String url ="http://finance.jrj.com.cn/tech/2019/04/18163027422690.shtml";
			cir.checkUrl(url);
		} catch (Exception e) {
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
