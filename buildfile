require 'buildr/git_auto_version'
require 'buildr/gpg'
require 'buildr/custom_pom'
require 'buildr/gwt'

desc 'GWT WebPoller Library'
define 'gwt-webpoller' do
  project.group = 'org.realityforge.gwt.webpoller'
  compile.options.source = '1.7'
  compile.options.target = '1.7'
  compile.options.lint = 'all'

  project.version = ENV['PRODUCT_VERSION'] if ENV['PRODUCT_VERSION']

  pom.add_apache_v2_license
  pom.add_github_project('realityforge/gwt-webpoller')
  pom.add_developer('realityforge', 'Peter Donald')
  pom.provided_dependencies.concat [:javax_annotation, :gwt_user, :javaee]

  compile.with :javax_annotation, :gwt_user, :javaee

  test.using :testng
  test.with :mockito

  gwt(%w(org.realityforge.gwt.webpoller.WebPoller),
      :java_args => %w(-Xms512M -Xmx1024M -XX:PermSize=128M -XX:MaxPermSize=256M),
      :draft_compile => 'true') unless ENV['GWT_COMPILE'] == 'no'

  package(:jar).include("#{_(:source, :main, :java)}/*")
  package(:sources)
  package(:javadoc)
end
