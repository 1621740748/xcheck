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

import static io.webfolder.cdp.event.Events.NetworkLoadingFinished;
import static io.webfolder.cdp.event.Events.NetworkResponseReceived;
import static java.util.Arrays.asList;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import io.webfolder.cdp.Launcher;
import io.webfolder.cdp.event.network.LoadingFinished;
import io.webfolder.cdp.event.network.ResponseReceived;
import io.webfolder.cdp.session.Session;
import io.webfolder.cdp.session.SessionFactory;
import io.webfolder.cdp.type.network.Response;
/**
 * 获取某个页面加载的url并按照host排序打印
 * @author huangyan
 *
 */
public class GetOuterStaticUrl {

	public static void main(String[] args) {

		Launcher launcher = new Launcher();

		Set<String> finished = new HashSet<>();
        List<String> urlList=new LinkedList<>();
		try (SessionFactory factory = launcher
				.launch(asList(
						"--headless"
						, "--disable-gpu", 
						"--allow-running-insecure-content"));
				Session session = factory.create()) {
			session.getCommand().getNetwork().enable();
			session.addEventListener((e, d) -> {
				if (NetworkLoadingFinished.equals(e)) {
					LoadingFinished lf = (LoadingFinished) d;
					finished.add(lf.getRequestId());
				}
				if (NetworkResponseReceived.equals(e)) {
					ResponseReceived rr = (ResponseReceived) d;
					Response response = rr.getResponse();
					urlList.add(response.getUrl());
				}
			});
			session.navigate("http://fund.jrj.com.cn/2010/12/2917308880729.shtml");
			session.waitDocumentReady();
		    session.wait(2000);
		    session.close();
		    urlList.stream().sorted().forEach(url->{
		    	System.out.println(url);
		    });
		}
	}
}
