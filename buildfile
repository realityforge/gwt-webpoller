require 'buildr/git_auto_version'
require 'buildr/gpg'
require 'buildr/gwt'

desc 'GWT WebPoller Library'
define 'gwt-webpoller' do
  project.group = 'org.realityforge.gwt.webpoller'
  compile.options.source = '1.8'
  compile.options.target = '1.8'
  compile.options.lint = 'all'

  project.version = ENV['PRODUCT_VERSION'] if ENV['PRODUCT_VERSION']

  pom.add_apache_v2_license
  pom.add_github_project('realityforge/gwt-webpoller')
  pom.add_developer('realityforge', 'Peter Donald')
  pom.provided_dependencies.concat [:javax_annotation, :gwt_user, :javaee]

  compile.with :jsinterop_base,
               :elemental2_core,
               :elemental2_dom,
               :elemental2_promise,
               :javax_annotation,
               :gwt_user,
               :javaee

  test.using :testng
  test.with :mockito

  package(:jar).include("#{_(:source, :main, :java)}/*")
  package(:sources)
  package(:javadoc)

  gwt_enhance(project)
end
