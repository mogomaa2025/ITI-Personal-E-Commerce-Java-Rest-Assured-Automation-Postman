
// Parse response JSON safely
let responseData = {};
let jsonValid = true;
try {
    responseData = pm.response.json();
} catch (e) {
    jsonValid = false;
}

pm.test("Response is valid JSON", function () {
    pm.expect(jsonValid).to.be.true;
});

// Handle 200 case (users listed)
if (pm.response.code === 200) {
    pm.test("200 OK: users listed and data valid", function () {
        if (!jsonValid) pm.expect.fail("Expected JSON body for 200 response, but parsing failed");

        pm.expect(responseData).to.have.property("success", true);
        pm.expect(responseData).to.have.property("data").that.is.an("array");

        // optional: check count exists and matches length
        if (responseData.hasOwnProperty("count")) {
            pm.expect(responseData.count).to.be.a("number");
            pm.expect(responseData.count).to.eql(responseData.data.length);
        }

        // check at least one user object has required fields
        if (responseData.data.length > 0) {
            let sample = responseData.data[0];
            pm.expect(sample).to.have.property("id").that.is.a("number");
            pm.expect(sample).to.have.property("email").that.is.a("string");
            pm.expect(sample).to.have.property("name").that.is.a("string");
            pm.expect(sample).to.have.property("is_admin").that.is.a("boolean");
        } else {
            pm.expect.fail("Expected at least one user in data array");
        }
    });

    // Save last user's id as collection variable
    let dataArr = responseData.data;
    let userID = Array.isArray(dataArr) && dataArr.length > 0 ? dataArr[dataArr.length - 1].id : undefined;
    if (typeof userID !== "undefined") {
        pm.collectionVariables.set("user_id", userID);
        console.log("Saved user_id:", userID);
    } else {
        console.warn("No user_id to save (data array empty)");
    }
}

// Unexpected status
else {
    pm.test("Unexpected status code", function () {
        pm.expect.fail("Unexpected status: " + pm.response.code);
    });
}