package org.ro.urls

import org.ro.core.Session
import org.ro.org.ro.core.observer.ActionObserver
import org.ro.to.*
import kotlin.test.BeforeTest

/**
 * This is an integration test that requires SimpleApp running on http://localhost:8080
 *
 * @item Iterate over all Objects defined in package 'urls',
 * @item check if the href 'self' can be invoked and
 * @item compare the response of invoking 'self' with what is hardcoded
 */
class UrlsTest {

    var link: Link? = null;
    var urls = mutableMapOf<String, String>()

    @BeforeTest
    fun setUp() {
        Session.login("http://localhost:8080", "sven", "pass")
        urls.put(FR_OBJECT.url, FR_OBJECT.str)
        urls.put(FR_OBJECT_BAZ.url, FR_OBJECT_BAZ.str)
        urls.put(FR_OBJECT_LAYOUT.url, FR_OBJECT_LAYOUT.str)
        urls.put(FR_OBJECT_PROPERTY.url, FR_OBJECT_PROPERTY.str)
        urls.put(FR_PROPERTY_DESCRIPTION.url, FR_PROPERTY_DESCRIPTION.str)
        urls.put(SO_0.url, SO_0.str)
        urls.put(SO_LIST_ALL.url, SO_LIST_ALL.str)
        urls.put(SO_LIST_ALL_INVOKE.url, SO_LIST_ALL_INVOKE.str)
        urls.put(SO_LIST_ALL_OBJECTS.url, SO_LIST_ALL_OBJECTS.str)
        urls.put(SO_MENU.url, SO_MENU.str)
        urls.put(SO_OBJECT_LAYOUT.url, SO_OBJECT_LAYOUT.str)
        urls.put(RESTFUL_SERVICES.url, RESTFUL_SERVICES.str)
    }

 //   @Test
    fun testUrls() {
        for (entry in urls) {
            val href = entry.key
            val link = Link(method = Method.GET.operation, href = href)
            ActionObserver().invoke(link)
        }
    }

}
