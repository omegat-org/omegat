import org.omegat.core.data.SourceTextEntry
import org.omegat.core.data.TMXEntry
import org.omegat.gui.issues.*

class ScriptIssue extends SimpleIssue {
	ScriptIssue(SourceTextEntry ste, TMXEntry te, ResourceBundle res) {
		super(ste, te)
		this.res = res
	}
	ResourceBundle res
	String getTypeName() { res.getString('typeName') }
	String getDescription() { 'Y U HAVE FOO NO BAR?' }
	String getColor() { '#00FF00' }
}

def hasProblem(String source, String target) {
	target.contains('foo') && !target.contains('bar')
}

IssueProviders.addIssueProvider([
	getName: { 'Foo Issues' },
	getId: { 'com.example.foo' },
	getIssues: { sourceEntry, tmxEntry ->
		if (hasProblem(sourceEntry.srcText, tmxEntry.translation)) {
			[new ScriptIssue(sourceEntry, tmxEntry, res)]
		} else {
			[]
		}
	}] as IIssueProvider)
