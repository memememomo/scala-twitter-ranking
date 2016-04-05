var CreateKeywordForm = React.createClass({
    handleSubmit: function(e) {
        e.preventDefault();
        var url = $("#create-keyword-form").data("url");
        var keyword = ReactDOM.findDOMNode(this.refs.keyword).value.trim();
        $.ajax({
            url: url,
            dataType: "json",
            type: "POST",
            data: {keyword: keyword},
            success: function(data) {
                console.log(data);
            }.bind(this),
            error: function(xhr, status, err) {
                console.log(err);
            }.bind(this)
        });
    },
    render: function() {
        return (
            <form className="createKeywordForm" onSubmit={this.handleSubmit}>
                <input type="text" placeholder="Keyword" ref="keyword"/>
                <input type="submit" value="Create" />
            </form>
        );
    }
});

React.render(
    <CreateKeywordForm />,
    document.getElementById("create-keyword-form")
);