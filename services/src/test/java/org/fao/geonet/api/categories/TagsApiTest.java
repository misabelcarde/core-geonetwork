package org.fao.geonet.api.categories;

import com.google.gson.FieldNamingStrategy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import junit.framework.Assert;
import org.fao.geonet.api.groups.GroupsApiTest;
import org.fao.geonet.domain.MetadataCategory;
import org.fao.geonet.domain.User;
import org.fao.geonet.repository.MetadataCategoryRepository;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.lang.reflect.Field;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test class for TagsApi.
 *
 * @author Jose García
 */
public class TagsApiTest extends AbstractServiceIntegrationTest {
    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private MetadataCategoryRepository _categoriesRepo;

    private MockMvc mockMvc;

    private MockHttpSession mockHttpSession;

    @Test
    public void getTags() throws Exception {
        List<MetadataCategory> categories = _categoriesRepo.findAll();

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        this.mockMvc.perform(get("/api/tags")
            .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(categories.size())));
    }

    @Test
    public void getTag() throws Exception {
        MetadataCategory category = _categoriesRepo.findOne(1);
        Assert.assertNotNull(category);

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        this.mockMvc.perform(get("/api/tags/" + category.getId())
            .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name", is(category.getName())));

    }

    @Test
    public void getNonExistingTag() throws Exception {
        MetadataCategory category = _categoriesRepo.findOne(222);
        Assert.assertNull(category);

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        MvcResult result = this.mockMvc.perform(get("/api/tags/222")
            .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().is(404))
            .andReturn();

        System.out.println(result.getResponse().getContentAsString());
    }

    @Test
    public void deleteTag() throws Exception {
        MetadataCategory category = _categoriesRepo.findOne(1);
        Assert.assertNotNull(category);

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        this.mockMvc.perform(delete("/api/tags/" + category.getId())
            .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().is(204));
    }

    @Test
    public void deleteNonExistingTag() throws Exception {
        MetadataCategory category = _categoriesRepo.findOne(222);
        Assert.assertNull(category);

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        this.mockMvc.perform(delete("/api/tags/222")
            .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().is(404));
    }

    @Test
    public void putTag() throws Exception {
        MetadataCategory category = _categoriesRepo.findOneByName("newcategory");
        Assert.assertNull(category);

        MetadataCategory newCategory = new MetadataCategory();
        newCategory.setId(-99);
        newCategory.setName("newcategory");

        Gson gson = new GsonBuilder()
            .setFieldNamingStrategy(new TagFieldNamingStrategy())
            .create();
        String json = gson.toJson(newCategory);

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        this.mockHttpSession = loginAsAdmin();

        this.mockMvc.perform(put("/api/tags")
            .content(json)
            .contentType(MediaType.APPLICATION_JSON)
            .session(this.mockHttpSession)
            .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().is(204));

        MetadataCategory categoryCreated = _categoriesRepo.findOneByName(newCategory.getName());
        Assert.assertNotNull(categoryCreated);
    }

    @Test
    public void updateTag() throws Exception {
        MetadataCategory category = _categoriesRepo.findOne(1);
        Assert.assertNotNull(category);

        category.setName(category.getName() + "-2");

        Gson gson = new GsonBuilder()
            .setFieldNamingStrategy(new TagFieldNamingStrategy())
            .create();
        String json = gson.toJson(category);

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        this.mockMvc.perform(put("/api/tags/" + category.getId())
            .content(json)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().is(204));
    }

    @Test
    public void updateNonExistingTag() throws Exception {
        MetadataCategory category = _categoriesRepo.findOne(222);
        Assert.assertNull(category);

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        this.mockMvc.perform(delete("/api/tags/222")
            .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().is(404));
    }

    /**
     * Strategy for Gson to remove _ from field names when serializing to JSON.
     */
    private class TagFieldNamingStrategy implements FieldNamingStrategy {
        @Override
        public String translateName(Field field) {
            return field.getName().replace("_", "");
        }

    }

}