package ai.saiy.android.amazon.directives;

import com.google.gson.annotations.SerializedName;

public class StructuralDirective {
    private DirectiveType directiveType;

    private String directiveParent;
    @SerializedName("directive")
    private final Directive directive;

    public StructuralDirective(Directive directive) {
        this.directive = directive;
    }

    public Directive getDirective() {
        return this.directive;
    }

    public void setDirectiveType(DirectiveType directiveType) {
        this.directiveType = directiveType;
    }

    public void setDirectiveParent(String directiveParent) {
        this.directiveParent = directiveParent;
    }

    public String getDirectiveParent() {
        return this.directiveParent;
    }

    public DirectiveType getDirectiveType() {
        return this.directiveType != null ? this.directiveType : DirectiveType.DIRECTIVE_NONE;
    }
}
